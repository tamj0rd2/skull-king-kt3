package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.PlaceABid
import com.tamj0rd2.skullking.domain.game.GameEvent.BidPlaced
import dev.forkhandles.values.random
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@Nested
class PlacingABid {
    @Test
    fun `when a bid is placed, a BidPlacedEvent is emitted`() {
        val command =
            PlaceABid(
                bid = Bid.of(1),
                actor = PlayerId.random(),
            )

        val game = Game(somePlayers)
        game.mustExecute(command)

        val bidPlacedEvent =
            game.state.events
                .filterIsInstance<BidPlaced>()
                .single()
        expectThat(bidPlacedEvent) {
            get { gameId }.isEqualTo(game.id)
            get { placedBy }.isEqualTo(command.actor)
            get { bid }.isEqualTo(command.bid)
        }
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
