package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameErrorCode.NotEnoughPlayersToStartGame
import com.tamj0rd2.skullking.domain.game.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.all
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty

class GameTest {
    companion object {
        val somePlayers = setOf(PlayerId.random(), PlayerId.random())

        fun Game.mustExecute(command: GameCommand) = execute(command).orThrow()
    }

    @Nested
    inner class StartingAGame {
        @Test
        fun `a new game starts with a GameStarted event`() {
            val game = Game(somePlayers)
            expectThat(game.events)
                .first()
                .isA<GameEvent.GameStarted>()
                .get { players }
                .isEqualTo(somePlayers)
        }

        @Test
        fun `cannot start a game without players`() {
            val players = emptySet<PlayerId>()
            expectThrows<NotEnoughPlayersToStartGame> { Game(players) }
        }

        @Test
        fun `cannot start a game with a single player`() {
            val players = setOf(PlayerId.random())
            expectThrows<NotEnoughPlayersToStartGame> { Game(players) }
        }
    }

    @Nested
    inner class StartingARound {
        @Test
        fun `when a round has started, each player is dealt 1 card, multiplied by the round number`() {
            val game = Game(somePlayers)
            game.mustExecute(GameCommand.StartRound(RoundNumber.of(1)))

            val roundStartedEvent = game.events.filterIsInstance<RoundStarted>().single()

            expectThat(roundStartedEvent) {
                get { roundNumber }.isEqualTo(RoundNumber.of(1))
                get { dealtCards.perPlayer.values }
                    .describedAs("cards dealt to each player")
                    .isNotEmpty()
                    .all { hasSize(roundStartedEvent.roundNumber.value) }
            }

            expectThat(game.state.cardsPerPlayer).isEqualTo(roundStartedEvent.dealtCards)
        }
    }

    @Nested
    inner class PlacingABid {
        @Test
        fun `when a bid is placed, a BidPlacedEvent is emitted`() {
            val command =
                GameCommand.PlaceABid(
                    bid = Bid.of(1),
                    actor = PlayerId.random(),
                )

            val game = Game(somePlayers)
            game.mustExecute(command)

            val bidPlacedEvent = game.events.filterIsInstance<BidPlaced>().single()
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
}
