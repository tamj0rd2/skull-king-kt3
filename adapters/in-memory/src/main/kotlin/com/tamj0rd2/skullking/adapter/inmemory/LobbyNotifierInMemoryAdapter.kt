package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.inandout.LobbyNotification
import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.application.port.output.LobbyNotifier
import com.tamj0rd2.skullking.domain.game.LobbyId

class LobbyNotifierInMemoryAdapter : LobbyNotifier {
    private val notifiers = mutableMapOf<LobbyId, LobbySpecificNotifier>()

    override fun subscribe(
        lobbyId: LobbyId,
        listener: LobbyNotificationListener,
    ) {
        getNotifierForLobby(lobbyId).addListenerAndSendMissedUpdates(listener)
    }

    override fun broadcast(
        lobbyId: LobbyId,
        updates: List<LobbyNotification>,
    ) {
        getNotifierForLobby(lobbyId).broadcast(updates)
    }

    private fun getNotifierForLobby(lobbyId: LobbyId) = notifiers[lobbyId] ?: LobbySpecificNotifier().also { notifiers[lobbyId] = it }

    private class LobbySpecificNotifier {
        private val listeners = mutableListOf<LobbyNotificationListener>()
        private val updates = mutableListOf<LobbyNotification>()

        fun addListenerAndSendMissedUpdates(listener: LobbyNotificationListener) {
            listeners += listener
            if (updates.isNotEmpty()) listener.receive(updates)
        }

        fun broadcast(newUpdates: List<LobbyNotification>) {
            updates.addAll(newUpdates)
            listeners.forEach { it.receive(newUpdates) }
        }
    }
}
