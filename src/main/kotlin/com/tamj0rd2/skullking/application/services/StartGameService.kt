package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.input.StartGameInput
import com.tamj0rd2.skullking.application.ports.input.StartGameOutput
import com.tamj0rd2.skullking.application.ports.input.StartGameUseCase
import com.tamj0rd2.skullking.application.repositories.GameRepository
import com.tamj0rd2.skullking.domain.game.GameCommand

class StartGameService(private val gameRepository: GameRepository) : StartGameUseCase {
    override fun execute(input: StartGameInput): StartGameOutput {
        val (game, version) = gameRepository.load(input.gameId)
        gameRepository.save(game.execute(GameCommand.StartGame), version)
        return StartGameOutput
    }
}
