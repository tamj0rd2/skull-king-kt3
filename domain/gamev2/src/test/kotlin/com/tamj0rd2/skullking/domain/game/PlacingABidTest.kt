package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.propertytesting.setSeed
import com.tamj0rd2.propertytesting.withIterations
import com.tamj0rd2.skullking.domain.game.GameCommand.PlaceABid
import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.game.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.game.values.Bid
import com.tamj0rd2.skullking.domain.game.values.RoundNumber
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.arbitrary
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
                player1 to APlacedBid(Bid.of(1)),
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
    fun `each player can only place up to 10 bids per game`() {
        propertyTest { statsReporter ->
            checkAll(Arb.game) { game ->
                game.state.players.forEach { player ->
                    // TODO: this is prone to failure. The generated BidCommand playerId isn't constrained to players in the game
                    val bidsPlacedByThisPlayer =
                        game.events
                            .filterIsInstance<BidPlaced>()
                            .count { it.placedBy == player }

                    assert(bidsPlacedByThisPlayer <= 10)
                }

                statsReporter.run {
                    game.events.forEach { EventTypeStatistics.classify(it) }
                    // TODO: there aren't enough events for this to be a useful test
                    EventCountStatistics.classify(game.events)
                }
            }
        }
    }

    // TODO: this test is slow as shit.
    @Test
    @Disabled
    fun `a player cannot place a bid more than once within the same round`() {
        propertyTest { statsReporter ->
            fun Game.aPlayerWhoHasAlreadyBid(rs: RandomSource) =
                state.players.filter { state.bids[it] is APlacedBid }.randomOrNull(rs.random)

            val gameWhereAPlayerHasAlreadyBid =
                arbitrary { rs ->
                    val game = Arb.game.filter { it.aPlayerWhoHasAlreadyBid(rs) != null }
                    game.bind()
                }

            checkAll(
                setSeed(-7475467121236708727).withIterations(1),
                gameWhereAPlayerHasAlreadyBid,
                Exhaustive.bid,
            ) { game, nextBidToPlace ->
                val aPlayerWhoHasAlreadyBidThisRound = game.aPlayerWhoHasAlreadyBid(randomSource())
                assume(aPlayerWhoHasAlreadyBidThisRound != null)

                val command = PlaceABid(nextBidToPlace, aPlayerWhoHasAlreadyBidThisRound!!)
                val commandResult = game.execute(command)
                assertIs<Failure<*>>(commandResult)

                statsReporter.run {
                    // TODO: add some checks for the specific error code i'm interested in
                    collect(commandResult::class.java.simpleName)
                }
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
