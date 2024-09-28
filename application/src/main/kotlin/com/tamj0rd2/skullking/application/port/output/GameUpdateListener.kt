package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.GameUpdate

fun interface GameUpdateListener {
    fun send(updates: List<GameUpdate>)

    fun send(vararg updates: GameUpdate) = send(updates.toList())
}
