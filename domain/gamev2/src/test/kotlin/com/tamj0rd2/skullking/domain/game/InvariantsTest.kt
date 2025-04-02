package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.PropertyTesting.gameInvariant
import com.tamj0rd2.skullking.domain.game.PropertyTesting.gameInvariantDeprecated
import com.tamj0rd2.skullking.domain.game.PropertyTesting.testInvariantHoldsWhenExecuting
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.result4k.peek
import dev.forkhandles.result4k.peekFailure
import io.kotest.property.assume
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InvariantsTest {
    @Test
    fun `a game always has 2-6 players`() {
        gameInvariant { game ->
            val currentPlayers = game.state.players
            assert(currentPlayers.size >= 2)
            assert(currentPlayers.size <= 6)
        }
    }

    @Test
    fun `the players in the game never change`() {
        gameInvariantDeprecated(validPlayerIdsArb) { initialPlayers, gameCommands ->
            val gameResult = Game.new(initialPlayers)
            assume(gameResult is Success)

            gameResult.orThrow().testInvariantHoldsWhenExecuting(gameCommands) { game ->
                assertEquals(initialPlayers, game.state.players)
            }
        }
    }

    @Test
    fun `every event in the game is related to that specific game`() {
        gameInvariantDeprecated(validPlayerIdsArb) { initialPlayers, gameCommands ->
            val initialGame = Game.new(initialPlayers).orThrow()
            val initialGameId = initialGame.id

            initialGame.testInvariantHoldsWhenExecuting(gameCommands) { game ->
                val eventGameIds =
                    game.state.events
                        .map { it.gameId }
                        .toSet()
                assert(eventGameIds.single() == initialGameId)
            }
        }
    }

    @Test
    fun `each successful command results in 1 event being emitted`() {
        gameInvariantDeprecated(validPlayerIdsArb) { initialPlayers, gameCommands ->
            val game = Game.new(initialPlayers).orThrow()

            gameCommands.forEach { command ->
                val eventsBeforeCommand = game.state.events

                game.execute(command).peek {
                    val eventsAfterCommand = game.state.events
                    assertEquals(eventsBeforeCommand, eventsAfterCommand.dropLast(1))
                }
            }
        }
    }

    @Test
    fun `a failed command does not append any events`() {
        gameInvariantDeprecated(validPlayerIdsArb) { initialPlayers, gameCommands ->
            val game = Game.new(initialPlayers).orThrow()

            gameCommands.forEach { command ->
                val eventsBeforeCommand = game.state.events

                game.execute(command).peekFailure {
                    val eventsAfterCommand = game.state.events
                    assertEquals(eventsBeforeCommand, eventsAfterCommand)
                }
            }
        }
    }

    @Test
    fun `a failed command does not modify the game's state`() {
        gameInvariantDeprecated(validPlayerIdsArb) { initialPlayers, gameCommands ->
            val game = Game.new(initialPlayers).orThrow()

            gameCommands.forEach { command ->
                val stateBeforeCommand = game.state

                game.execute(command).peekFailure {
                    val stateAfterCommand = game.state
                    assertEquals(stateBeforeCommand, stateAfterCommand)
                }
            }
        }
    }

    @Test
    @Disabled
    fun `a player can never have more than 10 cards`() {
        TODO()
    }
}
