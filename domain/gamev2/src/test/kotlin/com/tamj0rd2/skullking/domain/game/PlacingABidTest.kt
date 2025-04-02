package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.PlaceABid
import com.tamj0rd2.skullking.domain.game.GameEvent.BidPlaced
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class PlacingABidTest {
    @Test
    fun `when a bid is placed, a BidPlacedEvent is emitted`() {
        val command =
            PlaceABid(
                bid = Bid.of(1),
                actor = PlayerId.random(),
            )

        val game = Game.new(somePlayers).orThrow()
        game.mustExecute(command)

        val bidPlacedEvent =
            game.events
                .filterIsInstance<BidPlaced>()
                .single()
        assert(bidPlacedEvent.placedBy == command.actor)
        assert(bidPlacedEvent.bid == command.bid)
    }

    @Test
    @Disabled
    fun `cannot place a bid more than once within the same round`() {
        TODO("not yet implemented")
    }

    @Test
    @Disabled
    fun `cannot place a bid greater than the current round number`() {
        TODO("not yet implemented")
    }

    @Test
    @Disabled
    fun `cannot place a bid less than 0`() {
        TODO("not yet implemented")
    }
}
