package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.output.EventStoreSubscriber
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.domain.game.LobbyEvent
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.Version

class SendLobbyNotificationsService(
    private val lobbyRepository: LobbyRepository,
    private val lobbyNotifier: LobbyNotifier,
) : EventStoreSubscriber<LobbyId, LobbyEvent> {
    override fun onEventReceived(entityId: LobbyId, version: Version) {
        val lobby = lobbyRepository.load(entityId, version)
        val notifications = lobby.state.notifications
        // TODO: rename this to send
        lobbyNotifier.broadcast(entityId, notifications)
    }
}
