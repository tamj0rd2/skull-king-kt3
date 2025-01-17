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

    // TODO: this doesn't really belong with the use case. These should probably just be 2 separate types
    data class RevealedBid(
        override val madeBy: PlayerId,
        val bid: Bid,
    ) : PlacedBid {
        companion object {
            fun Bid.madeBy(playerId: PlayerId) = RevealedBid(playerId, this)
        }
    }

    data class UnknownBid(
        override val madeBy: PlayerId,
    ) : PlacedBid
}
