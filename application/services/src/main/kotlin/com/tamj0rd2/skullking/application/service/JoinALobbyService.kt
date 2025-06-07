package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase.JoinALobbyCommand
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase.JoinALobbyOutput
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.domain.game.LobbyCommand
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure

class JoinALobbyService(
    private val lobbyRepository: LobbyRepository,
    private val lobbyNotifier: LobbyNotifier,
) : JoinALobbyUseCase {
    override fun invoke(command: JoinALobbyCommand): Result4k<JoinALobbyOutput, LobbyErrorCode> {
        val game = lobbyRepository.load(command.lobbyId)
        game.execute(LobbyCommand.AddPlayer(command.playerId)).onFailure {
            return it
        }
        lobbyRepository.save(game)

        lobbyNotifier.subscribe(game.id, command.playerId, command.lobbyNotificationListener)
        return JoinALobbyOutput.asSuccess()
    }
}
