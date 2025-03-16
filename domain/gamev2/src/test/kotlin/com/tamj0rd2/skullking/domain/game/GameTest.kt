package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.game.GameEvent.CardPlayed
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickStarted
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
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
        @Disabled
        fun `cannot start a game without players`() {
            TODO("not yet implemented")
        }

        @Test
        @Disabled
        fun `cannot start a game with a single player`() {
            TODO("not yet implemented")
        }
    }

    @Nested
    inner class StartingARound {
        @Test
        fun `when a round has started, a round started event is emitted`() {
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

    @Nested
    inner class StartingATrick {
        @Test
        fun `when a trick is started, a TrickStartedEvent is emitted`() {
            val command =
                GameCommand.StartTrick(
                    trickNumber = TrickNumber.of(1),
                )

            val game = Game(somePlayers)
            game.mustExecute(command)

            val trickStartedEvent = game.events.filterIsInstance<TrickStarted>().single()
            expectThat(trickStartedEvent) {
                get { gameId }.isEqualTo(game.id)
                get { trickNumber }.isEqualTo(command.trickNumber)
            }
        }

        @Test
        @Disabled
        fun `cannot start a trick that has already started`() {
            TODO("not yet implemented")
        }
    }

    @Nested
    inner class PlayingACard {
        @Test
        fun `when a card is played, a CardPlayed event is emitted`() {
            val command =
                GameCommand.PlayACard(
                    card = CannedCard,
                    actor = PlayerId.random(),
                )

            val game = Game(somePlayers)
            game.mustExecute(command)

            val cardPlayedEvent = game.events.filterIsInstance<CardPlayed>().single()
            expectThat(cardPlayedEvent) {
                get { gameId }.isEqualTo(game.id)
                get { playedBy }.isEqualTo(command.actor)
                get { card }.isEqualTo(command.card)
            }
        }

        @Test
        @Disabled
        fun `a card can only be played during a trick`() {
            TODO("not yet implemented")
        }

        @Test
        @Disabled
        fun `a card can only be played by the player whose turn it is`() {
            TODO("not yet implemented")
        }
    }

    @Nested
    inner class CompletingATrick {
        @Test
        fun `when a trick is completed, a TrickCompleted event is emitted`() {
            val command =
                GameCommand.CompleteTrick(
                    trickNumber = TrickNumber.of(1),
                )

            val game = Game(somePlayers)
            game.mustExecute(command)

            val trickCompletedEvent = game.events.filterIsInstance<TrickCompleted>().single()
            expectThat(trickCompletedEvent) {
                get { gameId }.isEqualTo(game.id)
                get { trickNumber }.isEqualTo(command.trickNumber)
            }
        }

        @Test
        @Disabled
        fun `can only complete a trick if all players have played a card`() {
            TODO("not yet implemented")
        }

        @Test
        @Disabled
        fun `cannot complete a trick that has already been completed`() {
            TODO("not yet implemented")
        }

        @Test
        @Disabled
        fun `cannot complete a trick that hasn't started`() {
            TODO("not yet implemented")
        }
    }

    @Nested
    inner class CompletingARound {
        @Test
        fun `when a round is completed, a RoundCompleted event is emitted`() {
            val command =
                GameCommand.CompleteRound(
                    roundNumber = RoundNumber.of(1),
                )

            val game = Game(somePlayers)
            game.mustExecute(command)

            val roundCompletedEvent = game.events.filterIsInstance<RoundCompleted>().single()
            expectThat(roundCompletedEvent) {
                get { gameId }.isEqualTo(game.id)
                get { roundNumber }.isEqualTo(command.roundNumber)
            }
        }

        @Test
        @Disabled
        fun `can only complete a round if all tricks are complete`() {
            TODO("not yet implemented")
        }

        @Test
        @Disabled
        fun `cannot complete a round that has already been completed`() {
            TODO("not yet implemented")
        }

        @Test
        @Disabled
        fun `cannot complete a round that hasn't started`() {
            TODO("not yet implemented")
        }
    }
}
