package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.AddPlayerErrorCode.GameHasAlreadyStarted
import com.tamj0rd2.skullking.domain.model.game.Card
import com.tamj0rd2.skullking.domain.model.game.GameErrorCode
import com.tamj0rd2.skullking.domain.model.game.GameId
import com.tamj0rd2.skullking.domain.model.game.GameIsFull
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import com.tamj0rd2.skullking.domain.model.game.GameUpdate.CardDealt
import com.tamj0rd2.skullking.domain.model.game.GameUpdate.GameStarted
import com.tamj0rd2.skullking.domain.model.game.GameUpdate.PlayerJoined
import com.tamj0rd2.skullking.domain.model.game.PlayerHasAlreadyJoined
import com.tamj0rd2.skullking.domain.model.game.StartGameErrorCode.TooFewPlayers
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.JStringRepresentable
import com.ubertob.kondor.json.ObjectNodeConverter
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.obj
import com.ubertob.kondor.json.str
import org.http4k.asString
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.lens.BiDiWsMessageLens
import org.http4k.lens.ContentNegotiation
import org.http4k.lens.httpBodyRoot
import org.http4k.websocket.WsMessage

sealed interface Message

data object CreateNewGameMessage : Message

data class GameCreatedMessage(
    val gameId: GameId,
) : Message

data class JoinAcknowledgedMessage(
    val playerId: PlayerId,
) : Message

data class GameUpdateMessage(
    val gameUpdate: GameUpdate,
) : Message

data class ErrorMessage(
    val error: GameErrorCode,
) : Message

data object StartGameMessage : Message

val wsLens =
    BiDiWsMessageLens(
        get = { wsMessage -> JMessage.fromJson(wsMessage.bodyString()).orThrow() },
        setLens = { message, _ -> WsMessage(JMessage.toJson(message)) },
    )

val httpLens =
    httpBodyRoot(emptyList(), APPLICATION_JSON, ContentNegotiation.None)
        .map(
            nextIn = { JMessage.fromJson(it.payload.asString()).orThrow() },
            nextOut = { org.http4k.core.Body(JMessage.toJson(it)) },
        ).toLens()

private object JMessage : JSealed<Message>() {
    override val discriminatorFieldName: String = "type"

    override val subConverters: Map<String, ObjectNodeConverter<out Message>>
        get() =
            mapOf(
                "game-created" to JGameCreatedMessage,
                "create-game" to JSingleton(CreateNewGameMessage),
                "start-game" to JSingleton(StartGameMessage),
                "join-acknowledged" to JAcknowledged,
                "game-update" to JGameUpdateMessage,
                "error-message" to JErrorMessage,
            )

    override fun extractTypeName(obj: Message): String =
        when (obj) {
            is GameCreatedMessage -> "game-created"
            is CreateNewGameMessage -> "create-game"
            is JoinAcknowledgedMessage -> "join-acknowledged"
            is GameUpdateMessage -> "game-update"
            is StartGameMessage -> "start-game"
            is ErrorMessage -> "error-message"
        }
}

private object JPlayerId : JStringRepresentable<PlayerId>() {
    override val cons: (String) -> PlayerId = PlayerId.Companion::parse
    override val render: (PlayerId) -> String = PlayerId.Companion::show
}

private object JGameId : JStringRepresentable<GameId>() {
    override val cons: (String) -> GameId = GameId.Companion::parse
    override val render: (GameId) -> String = GameId.Companion::show
}

private object JGameCreatedMessage : JAny<GameCreatedMessage>() {
    private val gameId by str(JGameId, GameCreatedMessage::gameId)

    override fun JsonNodeObject.deserializeOrThrow() =
        GameCreatedMessage(
            gameId = +gameId,
        )
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
            )

    override fun extractTypeName(obj: GameUpdate): String =
        when (obj) {
            is PlayerJoined -> "player-joined"
            is GameStarted -> "game-started"
            is CardDealt -> "card-dealt"
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

private object JErrorMessage : JAny<ErrorMessage>() {
    private val reason by str(JErrorMessage::errorMessageAsString)

    private fun errorMessageAsString(errorMessage: ErrorMessage): String =
        when (errorMessage.error) {
            is GameIsFull -> "game-is-full"
            is TooFewPlayers -> "too-few-players"
            is PlayerHasAlreadyJoined -> "player-already-joined"
            is GameHasAlreadyStarted -> "game-already-started"
            else -> TODO("add support for ${errorMessage.error::class.java.simpleName}")
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

private class JSingleton<T : Any>(
    private val instance: T,
) : JAny<T>() {
    override fun JsonNodeObject.deserializeOrThrow() = instance
}
