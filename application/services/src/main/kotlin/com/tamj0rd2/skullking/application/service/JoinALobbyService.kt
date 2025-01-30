package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase.JoinALobbyCommand
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase.JoinALobbyOutput
import com.tamj0rd2.skullking.application.port.output.FindPlayerIdPort
import com.tamj0rd2.skullking.application.port.output.LobbyNotifier
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.application.port.output.SavePlayerIdPort
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.LobbyCommand
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.values.random

class JoinALobbyService(
    private val lobbyRepository: LobbyRepository,
    private val lobbyNotifier: LobbyNotifier,
    private val findPlayerIdPort: FindPlayerIdPort,
    private val savePlayerIdPort: SavePlayerIdPort,
) : JoinALobbyUseCase {
    override fun invoke(command: JoinALobbyCommand): Result4k<JoinALobbyOutput, LobbyErrorCode> {
        val playerId = findOrCreatePlayerId(command.sessionId)

        val game = lobbyRepository.load(command.lobbyId)
        game.execute(LobbyCommand.AddPlayer(playerId)).onFailure { return it }
        lobbyRepository.save(game)

        lobbyNotifier.subscribe(game.id, command.lobbyNotificationListener)
        lobbyNotifier.broadcast(game.id, game.state.notifications.sinceVersion(game.loadedAtVersion))

        return JoinALobbyOutput(playerId).asSuccess()
    }

    private fun findOrCreatePlayerId(sessionId: SessionId): PlayerId =
        findPlayerIdPort.findBy(sessionId)
            ?: PlayerId.random().also { savePlayerIdPort.save(sessionId, it) }
}
