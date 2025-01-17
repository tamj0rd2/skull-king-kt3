package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase.PlaceABidCommand
import com.tamj0rd2.skullking.domain.game.Bid
import dev.forkhandles.result4k.orThrow

internal class PlaceABidController(
    private val placeABidUseCase: PlaceABidUseCase,
) {
    operator fun invoke(
        playerSession: PlayerSession,
        bid: Bid,
    ) {
        val command = PlaceABidCommand(playerSession.gameId, playerSession.playerId, bid)
        placeABidUseCase.invoke(command).orThrow()
    }
}
