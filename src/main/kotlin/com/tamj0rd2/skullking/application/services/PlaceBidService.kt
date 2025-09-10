package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.input.PlaceBidInput
import com.tamj0rd2.skullking.application.ports.input.PlaceBidOutput
import com.tamj0rd2.skullking.application.ports.input.PlaceBidUseCase
import com.tamj0rd2.skullking.application.repositories.GameRepository
import com.tamj0rd2.skullking.domain.game.GameCommand

class PlaceBidService(private val gameRepository: GameRepository) : PlaceBidUseCase {
    override fun execute(input: PlaceBidInput): PlaceBidOutput {
        val (game, version) = gameRepository.load(input.gameId)
        gameRepository.save(game.execute(GameCommand.PlaceBid(input.playerId, input.bid)), version)
        return PlaceBidOutput
    }
}
