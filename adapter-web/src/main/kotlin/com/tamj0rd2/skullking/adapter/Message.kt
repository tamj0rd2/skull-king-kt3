package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateOutput
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.JStringRepresentable
import com.ubertob.kondor.json.ObjectNodeConverter
import com.ubertob.kondor.json.array
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.obj
import com.ubertob.kondor.json.str
import org.http4k.lens.BiDiWsMessageLens
import org.http4k.websocket.WsMessage

sealed interface Message

data class JoinAcknowledgedMessage(
    val playerId: PlayerId,
) : Message

data object GetGameStateMessage : Message

data class GameStateMessage(
    val state: ViewPlayerGameStateOutput,
) : Message

internal val wsLens
    get() =
        BiDiWsMessageLens(
            get = { wsMessage -> JMessage.fromJson(wsMessage.bodyString()).orThrow() },
            setLens = { message, _ -> WsMessage(JMessage.toJson(message)) },
        )

private object JMessage : JSealed<Message>() {
    override val discriminatorFieldName: String = "type"

    override val subConverters: Map<String, ObjectNodeConverter<out Message>>
        get() =
            mapOf(
                "join-acknowledged" to JAcknowledged,
                "get-game-state" to JSingleton(GetGameStateMessage),
                "game-state" to JGameState,
            )

    override fun extractTypeName(obj: Message): String =
        when (obj) {
            is JoinAcknowledgedMessage -> "join-acknowledged"
            is GetGameStateMessage -> "get-game-state"
            is GameStateMessage -> "game-state"
        }
}

private object JPlayerId : JStringRepresentable<PlayerId>() {
    override val cons: (String) -> PlayerId = PlayerId.Companion::parse
    override val render: (PlayerId) -> String = PlayerId.Companion::show
}

private object JAcknowledged : JAny<JoinAcknowledgedMessage>() {
    private val playerId by str(JPlayerId, JoinAcknowledgedMessage::playerId)

    override fun JsonNodeObject.deserializeOrThrow() =
        JoinAcknowledgedMessage(
            playerId = +playerId,
        )
}

class JSingleton<T : Any>(
    private val instance: T,
) : JAny<T>() {
    override fun JsonNodeObject.deserializeOrThrow() = instance
}

private object JGameState : JAny<GameStateMessage>() {
    private val state by obj(JViewPlayerGameStateOutput, GameStateMessage::state)

    override fun JsonNodeObject.deserializeOrThrow() =
        GameStateMessage(
            state = +state,
        )
}

private object JViewPlayerGameStateOutput : JAny<ViewPlayerGameStateOutput>() {
    private val players by array(JPlayerId, ViewPlayerGameStateOutput::players)

    override fun JsonNodeObject.deserializeOrThrow() =
        ViewPlayerGameStateOutput(
            players = +players,
        )
}
