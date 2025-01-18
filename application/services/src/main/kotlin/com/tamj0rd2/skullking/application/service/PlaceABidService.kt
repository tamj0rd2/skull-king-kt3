package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.ABidWasPlaced
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.AllBidsHaveBeenPlaced
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase.PlaceABidCommand
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase.PlaceABidOutput
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.domain.game.GameAction
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.orThrow

class PlaceABidService(
    private val gameUpdateNotifier: GameUpdateNotifier,
    private val gameRepository: GameRepository,
) : PlaceABidUseCase {
    override fun invoke(command: PlaceABidCommand): Result4k<PlaceABidOutput, GameErrorCode> {
        val game = gameRepository.load(command.gameId)
        game.execute(GameAction.PlaceBid(command.playerId, command.bid)).orThrow()
        gameRepository.save(game)

        gameUpdateNotifier.broadcast(command.gameId, ABidWasPlaced(command.playerId))

        if (game.state.allBidsHaveBeenPlaced) {
            gameUpdateNotifier.broadcast(command.gameId, AllBidsHaveBeenPlaced(game.state.bids))
        }

        return PlaceABidOutput.asSuccess()
    }
}
