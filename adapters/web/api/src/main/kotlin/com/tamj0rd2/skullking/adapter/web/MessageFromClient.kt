package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlaceABidMessage
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlayACardMessage
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.Card
import com.tamj0rd2.skullking.serialization.json.JBid
import com.tamj0rd2.skullking.serialization.json.JSingleton
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.ObjectNodeConverter
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.num
import com.ubertob.kondor.json.obj
import org.http4k.lens.BiDiWsMessageLens
import org.http4k.websocket.WsMessage

sealed interface MessageFromClient {
    data object StartGameMessage : MessageFromClient

    data class PlaceABidMessage(
        val bid: Bid,
    ) : MessageFromClient

    data class PlayACardMessage(
        val card: Card,
    ) : MessageFromClient
}

val messageFromClient =
    BiDiWsMessageLens(
        get = { wsMessage -> JMessageFromClient.fromJson(wsMessage.bodyString()).orThrow() },
        setLens = { message, _ -> WsMessage(JMessageFromClient.toJson(message)) },
    )

// TODO: make this work the same way as the serialization in the esdb adapter.
private object JMessageFromClient : JSealed<MessageFromClient>() {
    override val discriminatorFieldName: String = "type"

    override val subConverters: Map<String, ObjectNodeConverter<out MessageFromClient>> =
        mapOf(
            "start-game" to JSingleton(StartGameMessage),
            "place-a-bid" to JPlaceABidMessage,
            "play-a-card" to JPlayACardMessage,
        )

    override fun extractTypeName(obj: MessageFromClient): String =
        when (obj) {
            is StartGameMessage -> "start-game"
            is PlaceABidMessage -> "place-a-bid"
            is PlayACardMessage -> "play-a-card"
        }
}

private object JPlaceABidMessage : JAny<PlaceABidMessage>() {
    val bid by num(JBid, PlaceABidMessage::bid)

    override fun JsonNodeObject.deserializeOrThrow() = PlaceABidMessage(bid = +bid)
}

private object JPlayACardMessage : JAny<PlayACardMessage>() {
    val card by obj(JSingleton(Card), PlayACardMessage::card)

    override fun JsonNodeObject.deserializeOrThrow() = PlayACardMessage(card = +card)
}
