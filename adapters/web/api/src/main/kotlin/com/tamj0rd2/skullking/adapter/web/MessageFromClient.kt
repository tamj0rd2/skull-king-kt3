package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.serialization.json.JSingleton
import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.ObjectNodeConverter
import org.http4k.lens.BiDiWsMessageLens
import org.http4k.websocket.WsMessage

sealed interface MessageFromClient {
    data object StartGameMessage : MessageFromClient
}

val messageFromClient =
    BiDiWsMessageLens(
        get = { wsMessage -> JMessageFromClient.fromJson(wsMessage.bodyString()).orThrow() },
        setLens = { message, _ -> WsMessage(JMessageFromClient.toJson(message)) },
    )

private object JMessageFromClient : JSealed<MessageFromClient>() {
    override val discriminatorFieldName: String = "type"

    override val subConverters: Map<String, ObjectNodeConverter<out MessageFromClient>> =
        mapOf(
            "start-game" to JSingleton(StartGameMessage),
        )

    override fun extractTypeName(obj: MessageFromClient): String =
        when (obj) {
            is StartGameMessage -> "start-game"
        }
}
