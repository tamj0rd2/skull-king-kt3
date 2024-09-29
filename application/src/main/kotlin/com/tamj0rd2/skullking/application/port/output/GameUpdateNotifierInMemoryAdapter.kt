package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.GameUpdate

class GameUpdateNotifierInMemoryAdapter : GameUpdateNotifier {
    private val listeners = mutableListOf<GameUpdateListener>()

    override fun subscribe(listener: GameUpdateListener) {
        listeners.add(listener)
    }

    override fun broadcast(updates: List<GameUpdate>) {
        listeners.forEach { it.send(updates) }
    }
}
