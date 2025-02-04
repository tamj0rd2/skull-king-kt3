package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId

interface CreateNewLobbyUseCase {
    operator fun invoke(command: CreateNewLobbyCommand): CreateNewLobbyOutput

    data class CreateNewLobbyCommand(
        // TODO: remove
        val sessionId: SessionId,
        val playerId: PlayerId,
        val lobbyNotificationListener: LobbyNotificationListener,
    )

    data class CreateNewLobbyOutput(
        val lobbyId: LobbyId,
        // TODO: remove
        val playerId: PlayerId,
    )
}
