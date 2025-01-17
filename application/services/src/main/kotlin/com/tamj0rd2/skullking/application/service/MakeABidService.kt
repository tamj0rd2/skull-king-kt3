package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.AllBidsMade
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.BidMade
import com.tamj0rd2.skullking.application.port.input.MakeABidUseCase
import com.tamj0rd2.skullking.application.port.input.MakeABidUseCase.MakeABidCommand
import com.tamj0rd2.skullking.application.port.input.MakeABidUseCase.MakeABidOutput
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.domain.game.GameAction
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.orThrow

class MakeABidService(
    private val gameUpdateNotifier: GameUpdateNotifier,
    private val gameRepository: GameRepository,
) : MakeABidUseCase {
    override fun invoke(command: MakeABidCommand): Result4k<MakeABidOutput, GameErrorCode> {
        val game = gameRepository.load(command.gameId)
        game.execute(GameAction.PlaceBid(command.playerId, command.bid)).orThrow()
        gameRepository.save(game)

        gameUpdateNotifier.broadcast(command.gameId, BidMade(command.playerId))

        if (game.state.allBidsHaveBeenPlaced) {
            gameUpdateNotifier.broadcast(command.gameId, AllBidsMade(game.state.bids))
        }

        return MakeABidOutput.asSuccess()
    }
}
