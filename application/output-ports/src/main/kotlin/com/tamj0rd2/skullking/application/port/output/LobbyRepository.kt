package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.game.Lobby
import com.tamj0rd2.skullking.domain.game.LobbyEvent
import com.tamj0rd2.skullking.domain.game.LobbyId

class LobbyRepository(
    private val eventStore: EventStore<LobbyId, LobbyEvent>,
) {
    fun load(lobbyId: LobbyId): Lobby {
        val events = eventStore.read(lobbyId).ifEmpty { throw LobbyDoesNotExist() }
        return Lobby.from(events.toList())
    }

    fun save(lobby: Lobby) {
        eventStore.append(lobby.id, lobby.loadedAtVersion, lobby.newEventsSinceLobbyWasLoaded)
    }
}

class LobbyDoesNotExist : IllegalStateException()
