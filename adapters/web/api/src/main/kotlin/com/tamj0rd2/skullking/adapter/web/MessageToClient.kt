package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageToClient.ErrorMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.GameCreatedMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.GameUpdateMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.JoinAcknowledgedMessage
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.AllBidsMade
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.BidMade
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.CardDealt
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.GameStarted
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.PlayerJoined
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.GameHasAlreadyStarted
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.GameIsFull
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.PlayerHasAlreadyJoined
import com.tamj0rd2.skullking.domain.game.Card
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.domain.game.StartGameErrorCode.TooFewPlayers
import com.tamj0rd2.skullking.serialization.json.JBid
import com.tamj0rd2.skullking.serialization.json.JGameId
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
    data class GameCreatedMessage(
        val gameId: GameId,
        val playerId: PlayerId,
    ) : MessageToClient

    data class JoinAcknowledgedMessage(
        val playerId: PlayerId,
    ) : MessageToClient

    data class GameUpdateMessage(
        val gameUpdate: GameUpdate,
    ) : MessageToClient

    data class ErrorMessage(
        val error: GameErrorCode,
    ) : MessageToClient
}

val messageToClient =
    BiDiWsMessageLens(
        get = { wsMessage -> JMessageToClient.fromJson(wsMessage.bodyString()).orThrow() },
        setLens = { message, _ -> WsMessage(JMessageToClient.toJson(message)) },
    )

private object JGameCreatedMessage : JAny<GameCreatedMessage>() {
    private val gameId by str(JGameId, GameCreatedMessage::gameId)
    private val createdBy by str(JPlayerId, GameCreatedMessage::playerId)

    override fun JsonNodeObject.deserializeOrThrow() =
        GameCreatedMessage(
            gameId = +gameId,
            playerId = +createdBy,
        )
}

private object JMessageToClient : JSealed<MessageToClient>() {
    override val discriminatorFieldName: String = "type"

    override val subConverters: Map<String, ObjectNodeConverter<out MessageToClient>> =
        mapOf(
            "game-created" to JGameCreatedMessage,
            "join-acknowledged" to JAcknowledged,
            "game-update" to JGameUpdateMessage,
            "error-message" to JErrorMessage,
        )

    override fun extractTypeName(obj: MessageToClient): String =
        when (obj) {
            is GameCreatedMessage -> "game-created"
            is JoinAcknowledgedMessage -> "join-acknowledged"
            is GameUpdateMessage -> "game-update"
            is ErrorMessage -> "error-message"
        }
}

private object JAcknowledged : JAny<JoinAcknowledgedMessage>() {
    private val playerId by str(JPlayerId, JoinAcknowledgedMessage::playerId)

    override fun JsonNodeObject.deserializeOrThrow() =
        JoinAcknowledgedMessage(
            playerId = +playerId,
        )
}

private object JGameUpdateMessage : JAny<GameUpdateMessage>() {
    private val gameUpdate by obj(JGameUpdate, GameUpdateMessage::gameUpdate)

    override fun JsonNodeObject.deserializeOrThrow() = GameUpdateMessage(gameUpdate = +gameUpdate)
}

private object JGameUpdate : JSealed<GameUpdate>() {
    override val discriminatorFieldName: String = "type"

    override val subConverters: Map<String, ObjectNodeConverter<out GameUpdate>>
        get() =
            mapOf(
                "player-joined" to JPlayerJoined,
                "game-started" to JSingleton(GameStarted),
                "card-dealt" to JCardDealt,
                "bid-made" to JBidMade,
                "all-bids-made" to JAllBidsMade,
            )

    override fun extractTypeName(obj: GameUpdate): String =
        when (obj) {
            is PlayerJoined -> "player-joined"
            is GameStarted -> "game-started"
            is CardDealt -> "card-dealt"
            is BidMade -> "bid-made"
            is AllBidsMade -> "all-bids-made"
        }
}

private object JPlayerJoined : JAny<PlayerJoined>() {
    private val playerId by str(JPlayerId, PlayerJoined::playerId)

    override fun JsonNodeObject.deserializeOrThrow() = PlayerJoined(playerId = +playerId)
}

private object JCardDealt : JAny<CardDealt>() {
    private val card by obj(JSingleton(Card), CardDealt::card)

    override fun JsonNodeObject.deserializeOrThrow() = CardDealt(card = +card)
}

private object JBidMade : JAny<BidMade>() {
    private val playerId by str(JPlayerId, BidMade::playerId)

    override fun JsonNodeObject.deserializeOrThrow() = BidMade(playerId = +playerId)
}

private object JAllBidsMade : JAny<AllBidsMade>() {
    private val bids by obj(JMap(JPlayerId, JBid), AllBidsMade::bids)

    override fun JsonNodeObject.deserializeOrThrow() = AllBidsMade(bids = +bids)
}

private object JErrorMessage : JAny<ErrorMessage>() {
    private val reason by str(JErrorMessage::errorMessageAsString)

    private fun errorMessageAsString(errorMessage: ErrorMessage): String =
        when (errorMessage.error) {
            is GameIsFull -> "game-is-full"
            is TooFewPlayers -> "too-few-players"
            is PlayerHasAlreadyJoined -> "player-already-joined"
            is GameHasAlreadyStarted -> "game-already-started"
        }

    override fun JsonNodeObject.deserializeOrThrow(): ErrorMessage =
        ErrorMessage(
            when (val reason = +reason) {
                "game-is-full" -> GameIsFull()
                "too-few-players" -> TooFewPlayers()
                "player-already-joined" -> PlayerHasAlreadyJoined()
                "game-already-started" -> GameHasAlreadyStarted()
                else -> error("unknown error code - $reason")
            },
        )
}
