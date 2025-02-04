package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase
import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase.CreateNewLobbyCommand
import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase.CreateNewLobbyOutput
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.domain.game.Lobby

class CreateNewLobbyService(
    private val lobbyRepository: LobbyRepository,
    private val lobbyNotifier: LobbyNotifier,
) : CreateNewLobbyUseCase {
    override fun invoke(command: CreateNewLobbyCommand): CreateNewLobbyOutput {
        val lobby = Lobby.new(createdBy = command.playerId)
        lobbyRepository.save(lobby)
        lobbyNotifier.subscribe(lobby.id, command.lobbyNotificationListener)

        return CreateNewLobbyOutput(
            lobbyId = lobby.id,
        )
    }
}
