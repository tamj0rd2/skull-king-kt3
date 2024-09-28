package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.JStringRepresentable
import com.ubertob.kondor.json.ObjectNodeConverter
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
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

data class JoinGameMessage(
    val gameId: GameId,
) : Message

internal val wsLens =
    BiDiWsMessageLens(
        get = { wsMessage -> JMessage.fromJson(wsMessage.bodyString()).orThrow() },
        setLens = { message, _ -> WsMessage(JMessage.toJson(message)) },
    )

internal val httpLens =
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
                "join-acknowledged" to JAcknowledged,
                "join-game" to JJoinGameMessage,
            )

    override fun extractTypeName(obj: Message): String =
        when (obj) {
            is GameCreatedMessage -> "game-created"
            is CreateNewGameMessage -> "create-game"
            is JoinAcknowledgedMessage -> "join-acknowledged"
            is JoinGameMessage -> "join-game"
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

private object JJoinGameMessage : JAny<JoinGameMessage>() {
    private val gameId by str(JGameId, JoinGameMessage::gameId)

    override fun JsonNodeObject.deserializeOrThrow() =
        JoinGameMessage(
            gameId = +gameId,
        )
}

private class JSingleton<T : Any>(
    private val instance: T,
) : JAny<T>() {
    override fun JsonNodeObject.deserializeOrThrow() = instance
}
