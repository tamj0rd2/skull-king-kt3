package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.BidMade
import com.tamj0rd2.skullking.application.port.input.MakeABidUseCase
import com.tamj0rd2.skullking.application.port.input.MakeABidUseCase.MakeABidCommand
import com.tamj0rd2.skullking.application.port.input.MakeABidUseCase.MakeABidOutput
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import dev.forkhandles.result4k.Result4k

class MakeABidService(
    private val gameUpdateNotifier: GameUpdateNotifier,
) : MakeABidUseCase {
    override fun invoke(command: MakeABidCommand): Result4k<MakeABidOutput, GameErrorCode> {
        gameUpdateNotifier.broadcast(command.gameId, BidMade(command.playerId))
        // TODO: write a test to drive out changes to the model
        return MakeABidOutput.asSuccess()
    }
}
