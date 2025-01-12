package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.GameUpdate

class GameUpdateNotifierInMemoryAdapter : GameUpdateNotifier {
    private val notifiers = mutableMapOf<GameId, GameSpecificNotifier>()

    override fun subscribe(
        gameId: GameId,
        listener: GameUpdateListener,
    ) {
        getStorageForGame(gameId).addListenerAndSendMissedUpdates(listener)
    }

    override fun broadcast(
        gameId: GameId,
        updates: List<GameUpdate>,
    ) {
        getStorageForGame(gameId).broadcast(updates)
    }

    private fun getStorageForGame(gameId: GameId) = notifiers[gameId] ?: GameSpecificNotifier().also { notifiers[gameId] = it }

    private class GameSpecificNotifier {
        private val listeners = mutableListOf<GameUpdateListener>()
        private val updates = mutableListOf<GameUpdate>()

        fun addListenerAndSendMissedUpdates(listener: GameUpdateListener) {
            listeners += listener
            if (updates.isNotEmpty()) listener.receive(updates)
        }

        fun broadcast(newUpdates: List<GameUpdate>) {
            updates.addAll(newUpdates)
            listeners.forEach { it.receive(newUpdates) }
        }
    }
}
