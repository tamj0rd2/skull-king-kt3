package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.MakeABidUseCase
import com.tamj0rd2.skullking.application.port.input.MakeABidUseCase.MakeABidCommand
import com.tamj0rd2.skullking.domain.game.Bid
import dev.forkhandles.result4k.orThrow

internal class MakeABidController(
    private val makeABidUseCase: MakeABidUseCase,
) {
    operator fun invoke(
        playerSession: PlayerSession,
        bid: Bid,
    ) {
        val command = MakeABidCommand(playerSession.gameId, playerSession.playerId, bid)
        makeABidUseCase.invoke(command).orThrow()
    }
}
