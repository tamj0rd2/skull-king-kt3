package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.output.LobbyDoesNotExist
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.domain.game.Lobby
import com.tamj0rd2.skullking.domain.game.LobbyEvent
import com.tamj0rd2.skullking.domain.game.LobbyId

class LobbyRepositoryInMemoryAdapter : LobbyRepository {
    private val eventStore = EventStoreInMemoryAdapter<LobbyId, LobbyEvent>()

    override fun load(lobbyId: LobbyId): Lobby {
        val events = eventStore.read(lobbyId).ifEmpty { throw LobbyDoesNotExist() }
        return Lobby.from(events.toList())
    }

    override fun save(lobby: Lobby) {
        eventStore.append(lobby.id, lobby.loadedAtVersion, lobby.newEventsSinceLobbyWasLoaded)
    }
}
