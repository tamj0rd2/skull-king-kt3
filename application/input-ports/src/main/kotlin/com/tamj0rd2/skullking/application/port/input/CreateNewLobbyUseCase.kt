package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId

interface CreateNewLobbyUseCase {
    operator fun invoke(command: CreateNewLobbyCommand): CreateNewLobbyOutput

    data class CreateNewLobbyCommand(
        val playerId: PlayerId,
        val lobbyNotificationListener: LobbyNotificationListener,
    )

    data class CreateNewLobbyOutput(
        val lobbyId: LobbyId,
    )
}
