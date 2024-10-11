package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.game.GameUpdate

interface GameUpdateNotifier {
    fun subscribe(listener: GameUpdateListener)

    fun broadcast(updates: List<GameUpdate>)

    fun broadcast(vararg updates: GameUpdate) {
        require(updates.isNotEmpty()) { "list of updates was empty" }
        return broadcast(updates.toList())
    }
}
