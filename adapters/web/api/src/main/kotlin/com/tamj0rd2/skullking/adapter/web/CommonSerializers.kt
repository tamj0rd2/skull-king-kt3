package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.domain.game.PlayerId.Companion
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JStringRepresentable
import com.ubertob.kondor.json.jsonnode.JsonNodeObject

// TODO: this should be shared somewhere
internal object JPlayerId : JStringRepresentable<PlayerId>() {
    override val cons: (String) -> PlayerId = Companion::parse
    override val render: (PlayerId) -> String = Companion::show
}

// TODO: this should be shared somewhere
internal object JGameId : JStringRepresentable<GameId>() {
    override val cons: (String) -> GameId = GameId.Companion::parse
    override val render: (GameId) -> String = GameId.Companion::show
}

// TODO: this should be shared somewhere
internal class JSingleton<T : Any>(
    private val instance: T,
) : JAny<T>() {
    override fun JsonNodeObject.deserializeOrThrow() = instance
}
