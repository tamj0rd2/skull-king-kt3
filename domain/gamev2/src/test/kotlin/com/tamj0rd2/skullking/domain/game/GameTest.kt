package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameErrorCode.NotEnoughPlayersToStartGame
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
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
}
