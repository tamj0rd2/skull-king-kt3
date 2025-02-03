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

private object JMessageFromClient : JSealed<MessageFromClient>() {
    private val config =
        listOf(
            Triple(StartGameMessage::class, "start-game", JSingleton(StartGameMessage)),
            Triple(PlaceABidMessage::class, "place-a-bid", JPlaceABidMessage),
            Triple(PlayACardMessage::class, "play-a-card", JPlayACardMessage),
        )

    override val discriminatorFieldName: String = "type"

    override val subConverters: Map<String, ObjectNodeConverter<out MessageFromClient>> =
        config.associate { (_, eventType, converter) -> eventType to converter }

    override fun extractTypeName(obj: MessageFromClient): String {
        val configForThisObj = config.firstOrNull { (clazz, _, _) -> clazz == obj::class }
        checkNotNull(configForThisObj) { "Configure parsing for ${obj::class.java.simpleName}" }
        return configForThisObj.second
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
