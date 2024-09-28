package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.GameUpdate
import com.tamj0rd2.skullking.domain.model.PlayerId

class GameUpdateNotifierInMemoryAdapter : GameUpdateNotifier {
    private val listeners = mutableListOf<GameUpdateListener>()

    override fun subscribe(listener: GameUpdateListener) {
        listeners.add(listener)
    }

    override fun broadcast(updates: List<GameUpdate>) {
        listeners.forEach { it.send(updates) }
    }

    private data class Key(
        val gameId: GameId,
        val playerId: PlayerId,
    )
}
