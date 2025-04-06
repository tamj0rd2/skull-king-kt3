package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.result4k.orThrow
import dev.forkhandles.result4k.peek
import dev.forkhandles.result4k.peekFailure
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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
        gameStateInvariant { stateBeforeCommand, stateAfterCommand ->
            val playersBeforeCommand = stateBeforeCommand.players
            val playersAfterCommand = stateAfterCommand.players
            assertEquals(playersBeforeCommand, playersAfterCommand)
        }
    }

    @Test
    fun `the game's id never changes`() {
        gameInvariant { initialGameId: GameId, game ->
            assertEquals(initialGameId, game.id)
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
        gamePropertyTest(validPlayerIdsArb) { initialPlayers, gameCommands ->
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
        gamePropertyTest(validPlayerIdsArb) { initialPlayers, gameCommands ->
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
        gamePropertyTest(validPlayerIdsArb) { initialPlayers, gameCommands ->
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
        gamePropertyTest(validPlayerIdsArb) { initialPlayers, gameCommands ->
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
