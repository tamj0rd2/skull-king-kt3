package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.extensions.assertFailureIs
import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.PlaceABid
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.CannotBidOutsideBiddingPhase
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.gamev2.GamePhase.Bidding
import com.tamj0rd2.skullking.domain.gamev2.values.Bid
import com.tamj0rd2.skullking.domain.gamev2.values.RoundNumber
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.assume
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Nested
class PlaceABidTest {
    @Test
    fun `the state reflects each player's bid`() {
        val player1 = PlayerId.random()
        val player2 = PlayerId.random()

        val game =
            Game
                .new(setOf(player1, player2))
                .flatMap { it.execute(StartRound(RoundNumber.One)) }
                .flatMap { it.execute(PlaceABid(bid = Bid.One, actor = player1)) }
                .orThrow()

        assertEquals(
            (game.state.round as Round.InProgress).bids,
            mapOf(
                player1 to APlacedBid(Bid.One),
                player2 to OutstandingBid,
            ),
        )
    }

    @Test
    fun `cannot place a bid outside of the bidding phase`() {
        propertyTest {
            checkAll(Arb.game, Exhaustive.bid) { initial, bid ->
                assume(initial.state.phase != Bidding)

                val playerToBid = initial.state.players.random(randomSource().random)
                val command = PlaceABid(bid = bid, actor = playerToBid)
                assertFailureIs<CannotBidOutsideBiddingPhase>(initial.execute(command), "coming from phase ${initial.state.phase}")
            }
        }
    }

    @Test
    fun `the number of placed bids cannot exceed the number of players x10`() {
        propertyTest {
            checkAll(Arb.game) { game ->
                val playerCount = game.state.players.size
                val countOfPlacedBids = game.events.count { it is BidPlaced }
                assert(countOfPlacedBids <= playerCount * 10)
            }
        }
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
