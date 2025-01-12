package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageFromClient.MakeABidMessage
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.serialization.json.JBid
import com.tamj0rd2.skullking.serialization.json.JSingleton
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.ObjectNodeConverter
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.num
import org.http4k.lens.BiDiWsMessageLens
import org.http4k.websocket.WsMessage

sealed interface MessageFromClient {
    data object StartGameMessage : MessageFromClient

    data class MakeABidMessage(
        val bid: Bid,
    ) : MessageFromClient
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
            "make-a-bid" to JMakeABidMessage,
        )

    override fun extractTypeName(obj: MessageFromClient): String =
        when (obj) {
            is StartGameMessage -> "start-game"
            is MakeABidMessage -> "make-a-bid"
        }
}

private object JMakeABidMessage : JAny<MakeABidMessage>() {
    val bid by num(JBid, MakeABidMessage::bid)

    override fun JsonNodeObject.deserializeOrThrow() = MakeABidMessage(bid = +bid)
}
