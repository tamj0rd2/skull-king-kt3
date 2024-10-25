package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameOutput
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.domain.game.Game

class CreateNewGameService(
    private val gameRepository: GameRepository,
) : CreateNewGameUseCase {
    override fun invoke(command: CreateNewGameCommand): CreateNewGameOutput {
        val game = Game.new()
        gameRepository.save(game)
        return CreateNewGameOutput(game.id)
    }
}
