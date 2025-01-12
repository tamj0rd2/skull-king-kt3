package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k

interface MakeABidUseCase {
    data class MakeABidCommand(
        val gameId: GameId,
        val playerId: PlayerId,
        val bid: Bid,
    )

    data object MakeABidOutput

    operator fun invoke(command: MakeABidCommand): Result4k<MakeABidOutput, GameErrorCode>
}

sealed interface PlacedBid {
    val madeBy: PlayerId

    data class UnknownBid(
        override val madeBy: PlayerId,
    ) : PlacedBid
}
