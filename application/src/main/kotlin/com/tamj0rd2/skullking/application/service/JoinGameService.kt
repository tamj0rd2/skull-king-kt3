package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.domain.model.PlayerId
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random

class JoinGameService(
    private val gameRepository: GameRepository,
) : JoinGameUseCase {
    override fun invoke(command: JoinGameCommand): JoinGameOutput {
        val playerId = PlayerId.random()
        with(gameRepository) {
            val game = load(command.gameId)
            game.addPlayer(playerId).orThrow()
            save(game)
        }
        return JoinGameOutput(playerId)
    }
}
