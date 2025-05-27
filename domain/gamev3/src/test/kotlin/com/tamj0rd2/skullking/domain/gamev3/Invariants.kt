package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.skullking.domain.gamev3.GamePhase.AwaitingNextRound
import com.tamj0rd2.skullking.domain.gamev3.GamePhase.Bidding
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.assumeWasSuccessful
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.wasSuccessful
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.count
import strikt.assertions.filterIsInstance
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isIn
import strikt.assertions.isNotEqualTo

class Invariants {
    @Test
    fun `a valid game always has 2-6 players`() =
        propertyTest {
            checkAll(Arb.gameWithPotentiallyInvalidPlayers.validOnly()) { game ->
                expectThat(game.state.players.size).isIn(2..6)
            }
        }

    @Test
    fun `a game always start with a GameStarted event`() =
        propertyTest {
            checkAll(Arb.game.validOnly()) { game ->
                expectThat(game.events.first()).isA<GameStartedEvent>()
            }
        }

    @Test
    fun `a game only ever has 1 GameStarted event`() =
        propertyTest {
            checkAll(Arb.game.validOnly()) { game ->
                expectThat(game.events).filterIsInstance<GameStartedEvent>().count().isEqualTo(1)
            }
        }

    @Test
    fun `each successful command results in 1 new event being emitted`() =
        propertyTest {
            checkAll(Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                expectThat(updatedGame.events.size).isEqualTo(initialGame.events.size + 1)
            }
        }

    @Test
    fun `each successful command results in a state change`() =
        propertyTest {
            checkAll(Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                expectThat(updatedGame.state).isNotEqualTo(initialGame.state)
            }
        }

    @Test
    fun `successful commands never change the players in the game`() =
        propertyTest {
            checkAll(Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                expectThat(updatedGame.state.players).isEqualTo(initialGame.state.players)
            }
        }

    @Test
    fun `successful commands never change the game's ID`() =
        propertyTest {
            checkAll(Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                expectThat(updatedGame.id).isEqualTo(initialGame.id)
            }
        }

    @Test
    fun `successful commands never cause existing events to be changed or removed`() =
        propertyTest {
            checkAll(Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                expectThat(updatedGame.events.dropLast(1)).containsExactly(initialGame.events)
            }
        }

    @Test
    fun `a game reconstituted from events has the same identity, state and events as the game it was reconstituted from`() =
        propertyTest {
            checkAll(Arb.game.validOnly()) { originalGame ->
                expectThat(Game.reconstitute(originalGame.events)).wasSuccessful().and {
                    isEqualTo(originalGame)
                    get { id }.isEqualTo(originalGame.id)
                    get { state }.isEqualTo(originalGame.state)
                    get { events }.isEqualTo(originalGame.events)
                }
            }
        }

    // TODO: possibly put these in another file? Also double check my tests against what the book suggests for state machines

    @Test
    fun `a game in the AwaitingNextRound phase can only ever transition to Bidding`() =
        propertyTest {
            checkAll(
                // TODO: I'm using gameWithValidPlayers as a shortcut, otherwise the test would take way too long to run.
                //  check this in the book.
                Arb.game.validOnly().filter { it.state.phase is AwaitingNextRound },
                Arb.command,
            ) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                expectThat(updatedGame.state.phase).isA<Bidding>()
            }
        }
}
