package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.GameUpdate
import com.tamj0rd2.skullking.domain.model.PlayerId

class GameUpdateNotifierInMemoryAdapter : GameUpdateNotifier {
    private val listeners = mutableMapOf<Key, GameUpdateListener>()

    override fun subscribe(
        gameId: GameId,
        playerId: PlayerId,
        listener: GameUpdateListener,
    ) {
        listeners[Key(gameId, playerId)] = listener
    }

    override fun broadcast(updates: List<GameUpdate>) {
        listeners.values.forEach { it.send(updates) }
    }

    private data class Key(
        val gameId: GameId,
        val playerId: PlayerId,
    )
}
