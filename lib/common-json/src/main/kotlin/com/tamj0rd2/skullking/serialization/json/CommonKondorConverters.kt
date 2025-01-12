package com.tamj0rd2.skullking.serialization.json

import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.domain.game.PlayerId.Companion
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JInt
import com.ubertob.kondor.json.JNumRepresentable
import com.ubertob.kondor.json.JStringRepresentable
import com.ubertob.kondor.json.jsonnode.JsonNodeObject

object JPlayerId : JStringRepresentable<PlayerId>() {
    override val cons: (String) -> PlayerId = Companion::parse
    override val render: (PlayerId) -> String = Companion::show
}

object JGameId : JStringRepresentable<GameId>() {
    override val cons: (String) -> GameId = GameId.Companion::parse
    override val render: (GameId) -> String = GameId.Companion::show
}

object JBid : JNumRepresentable<Bid>() {
    override val cons: (Number) -> Bid = { Bid.of(JInt.cons(it)) }
    override val render: (Bid) -> Number = { JInt.render(it.value) }
}

class JSingleton<T : Any>(
    private val instance: T,
) : JAny<T>() {
    override fun JsonNodeObject.deserializeOrThrow() = instance
}
