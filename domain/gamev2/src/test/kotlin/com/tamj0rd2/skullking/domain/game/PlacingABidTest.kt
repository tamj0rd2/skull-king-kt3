package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.game.GameCommand.PlaceABid
import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.game.values.Bid
import com.tamj0rd2.skullking.domain.game.values.RoundNumber
import dev.forkhandles.result4k.Failure
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
import kotlin.test.assertIs

@Nested
class PlacingABidTest {
    @Test
    fun `the state reflects each player's bid`() {
        val game = Arb.newGame.filter { it.state.players.size == 2 }.next()
        game.execute(StartRound(RoundNumber.first)).orThrow()

        val player1 = game.state.players.first()
        val player2 = game.state.players.last()

        game.execute(PlaceABid(bid = Bid.of(1), actor = player1)).orThrow()
        assertEquals(
            game.state.bids,
            mapOf(
                player1 to PlacedBid(Bid.of(1)),
                player2 to OutstandingBid,
            ),
        )
    }

    @Test
    fun `cannot place a bid before round 1`() {
        propertyTest { statsReporter ->
            checkAll(Arb.game, Exhaustive.bid) { game, bid ->
                assume(game.state.roundNumber == RoundNumber.none)

                val playerToBid = game.state.players.random(randomSource().random)
                val command = PlaceABid(bid = bid, actor = playerToBid)
                assertIs<Failure<*>>(game.execute(command))

                statsReporter.run {
                }
            }
        }
    }

    @Test
    fun `cannot place a bid when the round is not in progress`() {
        propertyTest { statsReporter ->
            checkAll(Arb.game, Exhaustive.bid) { game, bid ->
                assume(!game.state.roundIsInProgress)

                val playerToBid = game.state.players.random(randomSource().random)
                val command = PlaceABid(bid = bid, actor = playerToBid)
                assertIs<Failure<*>>(game.execute(command))

                statsReporter.run {
                }
            }
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
