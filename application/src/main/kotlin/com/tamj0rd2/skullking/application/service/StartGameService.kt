package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameOutput
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.domain.model.game.GameErrorCode
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure

class StartGameService(
    private val gameRepository: GameRepository,
    private val gameUpdateNotifier: GameUpdateNotifier,
) : StartGameUseCase {
    override fun invoke(command: StartGameCommand): Result4k<StartGameOutput, GameErrorCode> {
        val game = gameRepository.load(command.gameId)
        game.start().onFailure { return it }
        gameRepository.save(game)

        gameUpdateNotifier.broadcast(GameUpdate.GameStarted)
        return StartGameOutput.asSuccess()
    }
}
