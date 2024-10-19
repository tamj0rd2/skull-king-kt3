package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.game.GameUpdate

fun interface GameUpdateListener {
    fun send(updates: List<GameUpdate>)

    fun send(vararg updates: GameUpdate) {
        require(updates.isNotEmpty()) { "must send at least 1 game update" }
        send(updates.toList())
    }
}
