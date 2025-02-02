package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.output.EventStoreSubscriber
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.domain.game.LobbyEvent
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.Version

class SendLobbyNotificationsService(
    private val lobbyRepository: LobbyRepository,
    // TODO: pretty sure LobbyNotifier can be made part of this service instead of being its own separate thing.
    private val lobbyNotifier: LobbyNotifier,
) : EventStoreSubscriber<LobbyId, LobbyEvent> {
    override fun onEventReceived(
        entityId: LobbyId,
        version: Version,
    ) {
        // TODO: I want to use a read model here instead.
        val lobby = lobbyRepository.load(entityId)
        val notifications = lobby.state.notifications.forVersion(version)
        lobbyNotifier.broadcast(entityId, notifications)
    }
}
