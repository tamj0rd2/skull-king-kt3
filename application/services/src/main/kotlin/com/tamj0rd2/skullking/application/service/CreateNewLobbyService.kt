package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase
import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase.CreateNewLobbyCommand
import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase.CreateNewLobbyOutput
import com.tamj0rd2.skullking.application.port.output.LobbyNotifier
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.application.port.output.SavePlayerIdPort
import com.tamj0rd2.skullking.domain.game.Lobby
import com.tamj0rd2.skullking.domain.game.LobbyNotification
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.values.random

class CreateNewLobbyService(
    private val lobbyRepository: LobbyRepository,
    private val lobbyNotifier: LobbyNotifier,
    private val savePlayerIdPort: SavePlayerIdPort,
) : CreateNewLobbyUseCase {
    override fun invoke(command: CreateNewLobbyCommand): CreateNewLobbyOutput {
        val playerId = PlayerId.random()
        savePlayerIdPort.save(command.sessionId, playerId)

        val lobby = Lobby.new(createdBy = playerId)
        lobbyRepository.save(lobby)
        lobbyNotifier.subscribe(lobby.id, command.lobbyNotificationListener)
        lobbyNotifier.broadcast(lobby.id, LobbyNotification.APlayerHasJoined(playerId))

        return CreateNewLobbyOutput(
            lobbyId = lobby.id,
            playerId = playerId,
        )
    }
}
