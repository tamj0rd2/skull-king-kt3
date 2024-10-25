package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.GameUpdate

class GameUpdateNotifierInMemoryAdapter : GameUpdateNotifier {
    private val storagePerGame = mutableMapOf<GameId, GameSpecificNotifier>()

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

    private fun getStorageForGame(gameId: GameId) = storagePerGame[gameId] ?: GameSpecificNotifier().also { storagePerGame[gameId] = it }

    private class GameSpecificNotifier {
        private val listeners = mutableListOf<GameUpdateListener>()
        private val updates = mutableListOf<GameUpdate>()

        fun addListenerAndSendMissedUpdates(listener: GameUpdateListener) {
            listeners += listener
            if (updates.isNotEmpty()) listener.send(updates)
        }

        fun broadcast(newUpdates: List<GameUpdate>) {
            updates.addAll(newUpdates)
            listeners.forEach { it.send(newUpdates) }
        }
    }
}
