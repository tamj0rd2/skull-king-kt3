package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.result4k.peek
import dev.forkhandles.result4k.peekFailure
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class InvariantsTest {
    @Test
    fun `a game always has 2-6 players`() {
        propertyTest {
            checkAll(Arb.game) { game ->
                val currentPlayers = game.state.players
                assert(currentPlayers.size >= 2)
                assert(currentPlayers.size <= 6)
            }
        }
    }

    @Test
    fun `the players in the game never change`() {
        propertyTest(CommandTypeStatistics, CommandExecutionStatistics) {
            checkAll(Arb.game, Arb.gameCommand) { game, command ->
                val initialPlayers = game.state.players
                val commandResult = game.execute(command)
                val playersNow = game.state.players
                assertEquals(initialPlayers, playersNow)

                CommandTypeStatistics.classify(command)
                CommandExecutionStatistics.classify(commandResult)
            }
        }
    }

    // TODO: having to add the statistic here and also in the checkAll part could easily lead to mistakes.
    @Test
    fun `the game's id never changes`() =
        propertyTest(CommandTypeStatistics, CommandExecutionStatistics) {
            checkAll(Arb.game, Arb.gameCommand) { game, command ->
                val initialGameId = game.id
                val commandResult = game.execute(command)
                val gameIdNow = game.id
                assertEquals(initialGameId, gameIdNow)

                CommandTypeStatistics.classify(command)
                CommandExecutionStatistics.classify(commandResult)
            }
        }

    @Test
    fun `every event in the game is related to that specific game`() {
        gameInvariant { initialGameId: GameId, game ->
            val gameIdsFromEvents =
                game.events
                    .map { it.gameId }
                    .toSet()
            assert(gameIdsFromEvents.single() == initialGameId)
        }
    }

    @Test
    fun `a game that is rebuilt from its history of events has the same identity, events and state as the original`() {
        gameInvariant { game ->
            val reconstitutedGame = Game.reconstituteFrom(game.events).orThrow()
            assertEquals(game, reconstitutedGame)
            assertEquals(game.id, reconstitutedGame.id)
            assertEquals(game.events, reconstitutedGame.events)
            assertEquals(game.state, reconstitutedGame.state)
        }
    }

    @Test
    fun `each successful command results in 1 event being emitted`() {
        @Suppress("DEPRECATION")
        gamePropertyTest(Arb.validPlayerIds) { initialPlayers, gameCommands ->
            val game = Game.new(initialPlayers).orThrow()

            gameCommands.forEach { command ->
                val eventsBeforeCommand = game.events

                game.execute(command).peek {
                    val eventsAfterCommand = game.events
                    assertEquals(eventsBeforeCommand.size + 1, eventsAfterCommand.size)
                    assertEquals(eventsBeforeCommand, eventsAfterCommand.dropLast(1))
                }
            }
        }
    }

    @Disabled
    @Test
    fun `each successful command results in a state change`() {
        @Suppress("DEPRECATION")
        gamePropertyTest(Arb.validPlayerIds) { initialPlayers, gameCommands ->
            val game = Game.new(initialPlayers).orThrow()

            gameCommands.forEach { command ->
                val stateBeforeCommand = game.state

                game.execute(command).peek {
                    val stateAfterCommand = game.state
                    assertNotEquals(stateBeforeCommand, stateAfterCommand)
                }
            }
        }
    }

    @Test
    fun `a failed command does not append any events`() {
        @Suppress("DEPRECATION")
        gamePropertyTest(Arb.validPlayerIds) { initialPlayers, gameCommands ->
            val game = Game.new(initialPlayers).orThrow()

            gameCommands.forEach { command ->
                val eventsBeforeCommand = game.events

                game.execute(command).peekFailure {
                    val eventsAfterCommand = game.events
                    assertEquals(eventsBeforeCommand, eventsAfterCommand)
                }
            }
        }
    }

    @Test
    fun `a failed command does not modify the game's state`() {
        @Suppress("DEPRECATION")
        gamePropertyTest(Arb.validPlayerIds) { initialPlayers, gameCommands ->
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
