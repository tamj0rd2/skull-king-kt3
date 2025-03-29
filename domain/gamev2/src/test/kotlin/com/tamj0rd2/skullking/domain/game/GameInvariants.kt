package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameErrorCode.NotEnoughPlayersToCreateGame
import com.tamj0rd2.skullking.domain.game.GameErrorCode.TooManyPlayersToCreateGame
import com.tamj0rd2.skullking.domain.game.GameEvent.GameStarted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.game.PropertyTesting.gameInvariant
import com.tamj0rd2.skullking.domain.game.PropertyTesting.gamePropertyTest
import com.tamj0rd2.skullking.domain.game.PropertyTesting.testInvariantHoldsWhenExecuting
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.failureOrNull
import dev.forkhandles.result4k.orThrow
import io.kotest.property.assume
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.first
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isLessThanOrEqualTo
import strikt.assertions.one
import strikt.assertions.size

class GameInvariants {
    @Test
    fun `a game always starts with a GameStarted event`() =
        gameInvariant { game ->
            expectThat(game.state.events).first().isA<GameStarted>()
        }

    @Test
    fun `the GameStarted event only ever appears once`() =
        gameInvariant { game ->
            expectThat(game.state.events).one { isA<GameStarted>() }
        }

    @Test
    fun `a game always has 2-6 players`() {
        gamePropertyTest(
            playerIdsArb = playerIdsArb,
            expectedClassifications = setOf(NotEnoughPlayersToCreateGame::class, TooManyPlayersToCreateGame::class, null),
        ) { playerIds, gameCommands ->
            val gameResult = Game.new(playerIds)
            collect(gameResult.failureOrNull()?.let { it::class })

            when {
                playerIds.size < Game.MINIMUM_PLAYER_COUNT -> {
                    expectThat(gameResult).isAGameErrorCodeOfType<NotEnoughPlayersToCreateGame>()
                }
                playerIds.size > Game.MAXIMUM_PLAYER_COUNT -> {
                    expectThat(gameResult).isAGameErrorCodeOfType<TooManyPlayersToCreateGame>()
                }
                else -> {
                    gameResult.orThrow().testInvariantHoldsWhenExecuting(gameCommands) { game ->
                        expectThat(game.state.players) {
                            size.isGreaterThanOrEqualTo(2)
                            size.isLessThanOrEqualTo(6)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `the players in the game never change`() {
        gamePropertyTest { initialPlayerIds, gameCommands ->
            val gameResult = Game.new(initialPlayerIds)
            assume(gameResult is Success)

            gameResult.orThrow().testInvariantHoldsWhenExecuting(gameCommands) { game ->
                expectThat(game.state.players).isEqualTo(initialPlayerIds)
            }
        }
    }

    @Test
    fun `a maximum of 10 rounds can be played`() =
        gameInvariant { game ->
            expectThat(
                game.state.events
                    .filterIsInstance<RoundStarted>()
                    .size,
            ).describedAs("RoundStarted event count")
                .isLessThanOrEqualTo(10)
            expectThat(
                game.state.events
                    .filterIsInstance<RoundCompleted>()
                    .size,
            ).describedAs("RoundCompleted event count")
                .isLessThanOrEqualTo(10)
        }

    @Test
    @Disabled
    fun `a player can never have more than 10 cards`() {
        TODO()
    }
}
