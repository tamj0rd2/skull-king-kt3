package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.LobbyNotification

class LobbyNotifier {
    private val notifiers = mutableMapOf<LobbyId, LobbySpecificNotifier>()

    fun subscribe(
        lobbyId: LobbyId,
        listener: LobbyNotificationListener,
    ) {
        getNotifierForLobby(lobbyId).addListenerAndSendMissedUpdates(listener)
    }

    fun broadcast(
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
            synchronized(this) {
                listeners += listener
            }

            if (updates.isNotEmpty()) listener.receive(updates)
        }

        fun broadcast(newUpdates: List<LobbyNotification>) {
            updates.addAll(newUpdates)

            synchronized(this) {
                listeners.forEach { it.receive(newUpdates) }
            }
        }
    }
}
