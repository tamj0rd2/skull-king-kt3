package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.propertytesting.setMaxDiscardPercentage
import com.tamj0rd2.skullking.domain.game.GameCommand.CompleteGame
import com.tamj0rd2.skullking.domain.game.GameCommand.CompleteRound
import com.tamj0rd2.skullking.domain.game.GameCommand.CompleteTrick
import com.tamj0rd2.skullking.domain.game.GameCommand.PlaceABid
import com.tamj0rd2.skullking.domain.game.GameCommand.PlayACard
import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.game.GameCommand.StartTrick
import com.tamj0rd2.skullking.domain.game.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.game.GameEvent.CardPlayed
import com.tamj0rd2.skullking.domain.game.GameEvent.GameCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickStarted
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.assume
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class InvariantsTest {
    @Test
    fun `a game always has 2-6 players`() =
        propertyTest {
            checkAll(Arb.game) { game ->
                val currentPlayers = game.state.players
                assert(currentPlayers.size >= 2)
                assert(currentPlayers.size <= 6)
            }
        }

    @Test
    fun `the players in the game never change`() =
        propertyTest { statsRecorder ->
            checkAll(Arb.game, Arb.gameCommand) { game, command ->
                val initialPlayers = game.state.players
                val commandResult = game.execute(command)
                val playersNow = game.state.players
                assertEquals(initialPlayers, playersNow)

                statsRecorder.run {
                    CommandTypeStatistics.classify(command)
                    CommandExecutionStatistics.classify(commandResult)
                }
            }
        }

    @Test
    fun `the game's id never changes`() =
        propertyTest { statsRecorder ->
            checkAll(Arb.game, Arb.gameCommand) { game, command ->
                val initialGameId = game.id
                val commandResult = game.execute(command)
                val gameIdNow = game.id
                assertEquals(initialGameId, gameIdNow)

                statsRecorder.run {
                    CommandTypeStatistics.classify(command)
                    CommandExecutionStatistics.classify(commandResult)
                }
            }
        }

    @Test
    fun `every event in the game is related to that specific game`() =
        propertyTest { statsRecorder ->
            checkAll(Arb.game) { game ->
                val gameIdsFromEvents =
                    game.events
                        .map { it.gameId }
                        .toSet()
                assert(gameIdsFromEvents.single() == game.id)

                statsRecorder.run {
                    // TODO: this test isn't really producing enough events. Most of the time there are only 1-10 which doesn't represent a normal game.
                    EventCountStatistics.classify(game.events)
                }
            }
        }

    @Test
    fun `a game that is rebuilt from its history of events has the same identity, events and state as the original`() =
        propertyTest { statsRecorder ->
            checkAll(Arb.game) { game ->
                val reconstitutedGame = Game.reconstituteFrom(game.events).orThrow()
                assertEquals(game, reconstitutedGame)
                assertEquals(game.id, reconstitutedGame.id)
                assertEquals(game.events, reconstitutedGame.events)
                assertEquals(game.state, reconstitutedGame.state)

                statsRecorder.run {
                    EventCountStatistics.classify(game.events)
                }
            }
        }

    @Test
    fun `each successful command results in 1 event being emitted`() {
        propertyTest { statsRecorder ->
            checkAll(setMaxDiscardPercentage(60), Arb.game, Arb.gameCommand) { game, command ->
                val eventsBeforeCommand = game.events
                assume(game.execute(command) is Success)
                val eventsAfterCommand = game.events

                assertEquals(eventsBeforeCommand.size + 1, eventsAfterCommand.size)
                assertEquals(eventsBeforeCommand, eventsAfterCommand.dropLast(1))
                assertEquals(
                    expected =
                        when (command) {
                            CompleteGame -> GameCompleted::class
                            is CompleteRound -> RoundCompleted::class
                            is CompleteTrick -> TrickCompleted::class
                            is PlaceABid -> BidPlaced::class
                            is PlayACard -> CardPlayed::class
                            is StartRound -> RoundStarted::class
                            is StartTrick -> TrickStarted::class
                        },
                    actual = eventsAfterCommand.last()::class,
                )

                statsRecorder.runCatching {
                    CommandTypeStatistics.classify(command)
                    EventCountStatistics.classify(eventsBeforeCommand)
                }
            }
        }
    }

    @Test
    @Disabled("just not implemented yet")
    fun `each successful command results in a state change`() {
        propertyTest { statsRecorder ->
            checkAll(Arb.game, Arb.gameCommand) { game, command ->
                val stateBeforeCommand = game.state
                assume(game.execute(command) is Success)
                val stateAfterCommand = game.state
                assertNotEquals(stateBeforeCommand, stateAfterCommand)

                statsRecorder.runCatching {
                    CommandTypeStatistics.classify(command)
                }
            }
        }
    }

    @Test
    fun `a failed command does not append any events`() {
        propertyTest { statsRecorder ->
            // TODO: reduce max discard percent by implementing more validation logic.
            checkAll(setMaxDiscardPercentage(75), Arb.game, Arb.gameCommand) { game, command ->
                val eventsBeforeCommand = game.events
                assume(game.execute(command) is Failure)
                val eventsAfterCommand = game.events
                assertEquals(eventsBeforeCommand, eventsAfterCommand)

                statsRecorder.runCatching {
                    // TODO: re-enable classifications once more validation logic has been implemented
                    // CommandTypeStatistics.classify(command)
                    EventCountStatistics.classify(eventsBeforeCommand)
                }
            }
        }
    }

    @Test
    fun `a failed command does not modify the game's state`() {
        propertyTest { statsRecorder ->
            // TODO: reduce max discard percent by implementing more validation logic.
            checkAll(setMaxDiscardPercentage(75), Arb.game, Arb.gameCommand) { game, command ->
                val stateBeforeCommand = game.state
                assume(game.execute(command) is Failure)
                val stateAfterCommand = game.state
                assertEquals(stateBeforeCommand, stateAfterCommand)

                statsRecorder.runCatching {
                    // TODO: re-enable classifications once more validation logic has been implemented
//                    CommandTypeStatistics.classify(command)
                }
            }
        }
    }

    @Test
    @Disabled("just not implemented yet")
    fun `a player can never have more than 10 cards`() {
        TODO()
    }
}
