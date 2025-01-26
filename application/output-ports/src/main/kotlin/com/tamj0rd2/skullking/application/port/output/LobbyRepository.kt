package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.game.Lobby
import com.tamj0rd2.skullking.domain.game.LobbyId

interface LobbyRepository {
    fun load(lobbyId: LobbyId): Lobby

    fun save(lobby: Lobby)
}

class LobbyDoesNotExist : IllegalStateException()
