package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageToClient.ErrorMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.JoinAcknowledgedMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.LobbyCreatedMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.LobbyNotificationMessage
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.GameHasAlreadyStarted
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.LobbyIsFull
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.PlayerHasAlreadyJoined
import com.tamj0rd2.skullking.domain.game.Card
import com.tamj0rd2.skullking.domain.game.GameNotInProgress
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.LobbyNotification
import com.tamj0rd2.skullking.domain.game.LobbyNotification.ABidWasPlaced
import com.tamj0rd2.skullking.domain.game.LobbyNotification.ACardWasDealt
import com.tamj0rd2.skullking.domain.game.LobbyNotification.ACardWasPlayed
import com.tamj0rd2.skullking.domain.game.LobbyNotification.APlayerHasJoined
import com.tamj0rd2.skullking.domain.game.LobbyNotification.AllBidsHaveBeenPlaced
import com.tamj0rd2.skullking.domain.game.LobbyNotification.TheGameHasStarted
import com.tamj0rd2.skullking.domain.game.LobbyNotification.TheTrickHasEnded
import com.tamj0rd2.skullking.domain.game.PlayedCard
import com.tamj0rd2.skullking.domain.game.StartGameErrorCode.TooFewPlayers
import com.tamj0rd2.skullking.serialization.json.JBid
import com.tamj0rd2.skullking.serialization.json.JLobbyId
import com.tamj0rd2.skullking.serialization.json.JPlayerId
import com.tamj0rd2.skullking.serialization.json.JSingleton
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JMap
import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.ObjectNodeConverter
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.obj
import com.ubertob.kondor.json.str
import org.http4k.lens.BiDiWsMessageLens
import org.http4k.websocket.WsMessage

sealed interface MessageToClient {
    data class LobbyCreatedMessage(
        val lobbyId: LobbyId,
    ) : MessageToClient

    data object JoinAcknowledgedMessage : MessageToClient

    data class LobbyNotificationMessage(
        val lobbyNotification: LobbyNotification,
    ) : MessageToClient

    data class ErrorMessage(
        val error: LobbyErrorCode,
    ) : MessageToClient
}

val messageToClient =
    BiDiWsMessageLens(
        get = { wsMessage -> JMessageToClient.fromJson(wsMessage.bodyString()).orThrow() },
        setLens = { message, _ -> WsMessage(JMessageToClient.toJson(message)) },
    )

private object JLobbyCreatedMessage : JAny<LobbyCreatedMessage>() {
    private val lobbyId by str(JLobbyId, LobbyCreatedMessage::lobbyId)

    override fun JsonNodeObject.deserializeOrThrow() =
        LobbyCreatedMessage(
            lobbyId = +lobbyId,
        )
}

private object JMessageToClient : JSealed<MessageToClient>() {
    private val config =
        listOf(
            Triple(LobbyCreatedMessage::class, "game-created", JLobbyCreatedMessage),
            Triple(JoinAcknowledgedMessage::class, "join-acknowledged", JSingleton(JoinAcknowledgedMessage)),
            Triple(LobbyNotificationMessage::class, "game-update", JLobbyNotificationMessage),
            Triple(ErrorMessage::class, "error-message", JErrorMessage),
        )

    override val subConverters: Map<String, ObjectNodeConverter<out MessageToClient>>
        get() = config.associate { (_, eventType, converter) -> eventType to converter }

    override fun extractTypeName(obj: MessageToClient): String {
        val configForThisObj = config.firstOrNull { (clazz, _, _) -> clazz == obj::class }
        checkNotNull(configForThisObj) { "Configure parsing for message to client ${obj::class.java.simpleName}" }
        return configForThisObj.second
    }
}

private object JLobbyNotificationMessage : JAny<LobbyNotificationMessage>() {
    private val lobbyNotification by obj(JLobbyNotification, LobbyNotificationMessage::lobbyNotification)

    override fun JsonNodeObject.deserializeOrThrow() = LobbyNotificationMessage(lobbyNotification = +lobbyNotification)
}

private object JLobbyNotification : JSealed<LobbyNotification>() {
    private val config =
        listOf(
            Triple(APlayerHasJoined::class, "player-joined", JPlayerJoined),
            Triple(TheGameHasStarted::class, "game-started", JSingleton(TheGameHasStarted)),
            Triple(ACardWasDealt::class, "card-dealt", JCardDealt),
            Triple(ABidWasPlaced::class, "bid-placed", JBidPlaced),
            Triple(AllBidsHaveBeenPlaced::class, "all-bids-placed", JAllBidsPlaced),
            Triple(ACardWasPlayed::class, "card-played", JCardPlayed),
            Triple(TheTrickHasEnded::class, "trick-ended", JTrickEnded),
        )

    override val subConverters: Map<String, ObjectNodeConverter<out LobbyNotification>>
        get() = config.associate { (_, eventType, converter) -> eventType to converter }

    override fun extractTypeName(obj: LobbyNotification): String {
        val configForThisObj = config.firstOrNull { (clazz, _, _) -> clazz == obj::class }
        checkNotNull(configForThisObj) { "Configure parsing for lobby notification type ${obj::class.java.simpleName}" }
        return configForThisObj.second
    }
}

private object JPlayerJoined : JAny<APlayerHasJoined>() {
    private val playerId by str(JPlayerId, APlayerHasJoined::playerId)

    override fun JsonNodeObject.deserializeOrThrow() = APlayerHasJoined(playerId = +playerId)
}

private object JCardDealt : JAny<ACardWasDealt>() {
    private val card by obj(JSingleton(Card), ACardWasDealt::card)

    override fun JsonNodeObject.deserializeOrThrow() = ACardWasDealt(card = +card)
}

private object JBidPlaced : JAny<ABidWasPlaced>() {
    private val playerId by str(JPlayerId, ABidWasPlaced::playerId)

    override fun JsonNodeObject.deserializeOrThrow() = ABidWasPlaced(playerId = +playerId)
}

private object JAllBidsPlaced : JAny<AllBidsHaveBeenPlaced>() {
    private val bids by obj(JMap(JPlayerId, JBid), AllBidsHaveBeenPlaced::bids)

    override fun JsonNodeObject.deserializeOrThrow() = AllBidsHaveBeenPlaced(bids = +bids)
}

private object JCardPlayed : JAny<ACardWasPlayed>() {
    private val playedCard by obj(JPlayedCard, ACardWasPlayed::playedCard)

    override fun JsonNodeObject.deserializeOrThrow() = ACardWasPlayed(playedCard = +playedCard)
}

private object JPlayedCard : JAny<PlayedCard>() {
    private val playedBy by obj(JPlayerId, PlayedCard::playedBy)
    private val card by obj(JSingleton(Card), PlayedCard::card)

    override fun JsonNodeObject.deserializeOrThrow() =
        PlayedCard(
            card = +card,
            playedBy = +playedBy,
        )
}

private object JTrickEnded : JAny<TheTrickHasEnded>() {
    private val winner by obj(JPlayerId, TheTrickHasEnded::winner)

    override fun JsonNodeObject.deserializeOrThrow() =
        TheTrickHasEnded(
            winner = +winner,
        )
}

private object JErrorMessage : JAny<ErrorMessage>() {
    private val reason by str(JErrorMessage::errorMessageAsString)

    private fun errorMessageAsString(errorMessage: ErrorMessage): String =
        when (errorMessage.error) {
            is LobbyIsFull -> "game-is-full"
            is TooFewPlayers -> "too-few-players"
            is PlayerHasAlreadyJoined -> "player-already-joined"
            is GameHasAlreadyStarted -> "game-already-started"
            is GameNotInProgress -> TODO()
        }

    override fun JsonNodeObject.deserializeOrThrow(): ErrorMessage =
        ErrorMessage(
            when (val reason = +reason) {
                "game-is-full" -> LobbyIsFull()
                "too-few-players" -> TooFewPlayers()
                "player-already-joined" -> PlayerHasAlreadyJoined()
                "game-already-started" -> GameHasAlreadyStarted()
                else -> error("unknown error code - $reason")
            },
        )
}
