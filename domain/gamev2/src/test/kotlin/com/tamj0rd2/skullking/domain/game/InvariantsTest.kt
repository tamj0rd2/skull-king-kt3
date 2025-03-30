package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameErrorCode.NotEnoughPlayersToCreateGame
import com.tamj0rd2.skullking.domain.game.GameErrorCode.TooManyPlayersToCreateGame
import com.tamj0rd2.skullking.domain.game.PropertyTesting.gameInvariant
import com.tamj0rd2.skullking.domain.game.PropertyTesting.testInvariantHoldsWhenExecuting
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.failureOrNull
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.assume
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InvariantsTest {
    @Test
    fun `a game always has 2-6 players`() {
        gameInvariant(
            playerIdsArb =
                Arb.choice(
                    validPlayerIdsArb,
                    potentiallyInvalidPlayerIdsArb,
                ),
        ) { players, gameCommands ->
            val gameResult = Game.new(players)

            when {
                players.size < 2 -> {
                    assert(gameResult.failureOrNull() is NotEnoughPlayersToCreateGame)
                }

                players.size > 6 -> {
                    assert(gameResult.failureOrNull() is TooManyPlayersToCreateGame)
                }

                else -> {
                    gameResult.orThrow().testInvariantHoldsWhenExecuting(gameCommands) { game ->
                        val currentPlayers = game.state.players
                        assert(currentPlayers.size >= 2)
                        assert(currentPlayers.size <= 6)
                    }
                }
            }
        }
    }

    @Test
    fun `the players in the game never change`() {
        gameInvariant(validPlayerIdsArb) { initialPlayers, gameCommands ->
            val gameResult = Game.new(initialPlayers)
            assume(gameResult is Success)

            gameResult.orThrow().testInvariantHoldsWhenExecuting(gameCommands) { game ->
                assertEquals(initialPlayers, game.state.players)
            }
        }
    }

    @Test
    @Disabled
    fun `a player can never have more than 10 cards`() {
        TODO()
    }
}
