package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.LobbyNotification
import com.tamj0rd2.skullking.domain.game.LobbyNotificationRecipient.Everyone
import com.tamj0rd2.skullking.domain.game.LobbyNotificationRecipient.Someone
import com.tamj0rd2.skullking.domain.game.PlayerId

class LobbyNotifier {
    private val notifiers = mutableMapOf<LobbyId, LobbySpecificNotifier>()

    fun subscribe(lobbyId: LobbyId, playerId: PlayerId, listener: LobbyNotificationListener) {
        getNotifierForLobby(lobbyId).addListenerAndSendMissedUpdates(playerId, listener)
    }

    fun broadcast(lobbyId: LobbyId, updates: List<LobbyNotification>) {
        getNotifierForLobby(lobbyId).send(updates)
    }

    private fun getNotifierForLobby(lobbyId: LobbyId) =
        notifiers[lobbyId] ?: LobbySpecificNotifier().also { notifiers[lobbyId] = it }

    private class LobbySpecificNotifier {
        private val listeners = mutableMapOf<PlayerId, LobbyNotificationListener>()
        private val updates = mutableListOf<LobbyNotification>()

        fun addListenerAndSendMissedUpdates(
            playerId: PlayerId,
            listener: LobbyNotificationListener,
        ) {
            synchronized(this) {
                require(playerId !in listeners) { "Listener already registered for $playerId" }
                listeners[playerId] = listener
            }

            if (updates.isNotEmpty()) listener.receive(updates)
        }

        fun send(newUpdates: List<LobbyNotification>) {
            updates.addAll(newUpdates.filter { it.recipient is Everyone })

            synchronized(this) {
                listeners.forEach { (playerId, listener) ->
                    listener.receive(newUpdates.intendedFor(playerId))
                }
            }
        }

        private fun List<LobbyNotification>.intendedFor(playerId: PlayerId) = filter {
            when (val recipient = it.recipient) {
                Everyone -> true
                is Someone -> recipient.playerId == playerId
            }
        }
    }
}
