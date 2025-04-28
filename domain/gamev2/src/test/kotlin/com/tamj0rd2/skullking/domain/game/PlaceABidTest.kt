package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.assertFailureIs
import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.game.GameCommand.PlaceABid
import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotBidOutsideBiddingPhase
import com.tamj0rd2.skullking.domain.game.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.game.GamePhase.Bidding
import com.tamj0rd2.skullking.domain.game.values.Bid
import com.tamj0rd2.skullking.domain.game.values.RoundNumber
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.next
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
        val game = Arb.newGame.filter { it.state.players.size == 2 }.next()
        game.execute(StartRound(RoundNumber.first)).orThrow()

        val player1 = game.state.players.first()
        val player2 = game.state.players.last()

        game.execute(PlaceABid(bid = Bid.one, actor = player1)).orThrow()
        assertEquals(
            game.state.round.bids,
            mapOf(
                player1 to APlacedBid(Bid.one),
                player2 to OutstandingBid,
            ),
        )
    }

    @Test
    fun `cannot place a bid outside of the bidding phase`() {
        propertyTest { statsRecorder ->
            checkAll(Arb.game, Exhaustive.bid) { game, bid ->
                assume(game.state.phase != Bidding)

                val playerToBid = game.state.players.random(randomSource().random)
                val command = PlaceABid(bid = bid, actor = playerToBid)
                assertFailureIs<CannotBidOutsideBiddingPhase>(game.execute(command), "coming from phase ${game.state.phase}")

                statsRecorder.run {
                    classify(game.state.phase::class.simpleName!!)
                }
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
