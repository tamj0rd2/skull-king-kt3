package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.GameUpdate

// TODO: move this to test fixtures if/when I create a real implementation for the server.
class GameUpdateNotifierInMemoryAdapter : GameUpdateNotifier {
    private val listeners = mutableListOf<GameUpdateListener>()

    override fun subscribe(listener: GameUpdateListener) {
        listeners.add(listener)
    }

    override fun broadcast(updates: List<GameUpdate>) {
        listeners.forEach { it.send(updates) }
    }
}
