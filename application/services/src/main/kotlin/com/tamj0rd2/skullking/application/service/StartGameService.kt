package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameOutput
import com.tamj0rd2.skullking.application.port.output.LobbyNotifier
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.domain.game.LobbyCommand
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure

class StartGameService(
    private val lobbyRepository: LobbyRepository,
    private val lobbyNotifier: LobbyNotifier,
) : StartGameUseCase {
    override fun invoke(command: StartGameCommand): Result4k<StartGameOutput, LobbyErrorCode> {
        val game = lobbyRepository.load(command.lobbyId)
        game.execute(LobbyCommand.Start).onFailure { return it }
        lobbyRepository.save(game)

        lobbyNotifier.broadcast(game.id, game.state.notifications.sinceVersion(game.loadedAtVersion))
        return StartGameOutput.asSuccess()
    }
}
