package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.output.LobbyDoesNotExist
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.domain.game.Lobby
import com.tamj0rd2.skullking.domain.game.LobbyEvent
import com.tamj0rd2.skullking.domain.game.LobbyId

class LobbyRepositoryInMemoryAdapter : LobbyRepository {
    private val savedEvents = mutableMapOf<LobbyId, List<LobbyEvent>>()

    override fun load(lobbyId: LobbyId): Lobby {
        val events = savedEvents[lobbyId] ?: throw LobbyDoesNotExist()
        return Lobby.from(events)
    }

    override fun save(lobby: Lobby) {
        savedEvents[lobby.id] = lobby.allEvents
    }
}
