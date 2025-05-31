package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.skullking.domain.gamev3.GameState.AwaitingNextRound
import com.tamj0rd2.skullking.domain.gamev3.GameState.Bidding
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.assumeWasSuccessful
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.printStatistics
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.propTestConfig
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.wasSuccessful
import io.kotest.property.Arb
import io.kotest.property.PropertyContext
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
    private companion object {
        @Suppress("NO_REFLECTION_IN_CLASS_PATH")
        val statesToCheck = GameState::class.sealedSubclasses.minus(GameState.NotStarted::class).map { it.simpleName }

        fun expectGameStates(): Map<Any?, Double> =
            statesToCheck
                .filter {
                    when (it) {
                        Bidding::class.simpleName -> false // TODO: Bidding state is wip. remove this line to help drive the behaviour.
                        else -> true
                    }
                }.associateWith { 95.00 / statesToCheck.size }

        fun PropertyContext.collectState(game: Game) {
            collect(game.state::class.simpleName)
        }
    }

    @Test
    fun `a game always has 2-6 players`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collectState(game)
                expectThat(game.deprecatedState.players.size).isIn(2..6)
            }.printStatistics().checkCoveragePercentages(expectGameStates())
        }

    @Test
    fun `a game always start with a GameStarted event`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collectState(game)
                expectThat(game.events.first()).isA<GameStartedEvent>()
            }.printStatistics().checkCoveragePercentages(expectGameStates())
        }

    @Test
    fun `a game only ever has 1 GameStarted event`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collectState(game)
                expectThat(game.events).filterIsInstance<GameStartedEvent>().count().isEqualTo(1)
            }.printStatistics().checkCoveragePercentages(expectGameStates())
        }

    @Test
    fun `each successful command results in 1 new event being emitted`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.events.size).isEqualTo(initialGame.events.size + 1)
            }.printStatistics().checkCoveragePercentages(expectGameStates())
        }

    @Test
    fun `each successful command results in a state change`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.deprecatedState).isNotEqualTo(initialGame.deprecatedState)
            }.printStatistics().checkCoveragePercentages(expectGameStates())
        }

    @Test
    fun `successful commands never change the players in the game`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.deprecatedState.players).isEqualTo(initialGame.deprecatedState.players)
            }.printStatistics().checkCoveragePercentages(expectGameStates())
        }

    @Test
    fun `successful commands never change the game's ID`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.id).isEqualTo(initialGame.id)
            }.printStatistics().checkCoveragePercentages(expectGameStates())
        }

    @Test
    fun `successful commands never cause existing events to be changed or removed`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.events.dropLast(1)).containsExactly(initialGame.events)
            }.printStatistics().checkCoveragePercentages(expectGameStates())
        }

    @Test
    fun `a game reconstituted from events has the same identity, state and events as the game it was reconstituted from`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { originalGame ->
                collectState(originalGame)
                expectThat(Game.reconstitute(originalGame.events)).wasSuccessful().and {
                    isEqualTo(originalGame)
                    get { id }.isEqualTo(originalGame.id)
                    get { events }.isEqualTo(originalGame.events)
                    get { deprecatedState }.isEqualTo(originalGame.deprecatedState)
                    get { state }.isEqualTo(originalGame.state)
                    get { state }.isEqualTo(originalGame.deprecatedState.state)
                }
            }.printStatistics().checkCoveragePercentages(expectGameStates())
        }

    // TODO: possibly put these in another file? Also double check my tests against what the book suggests for state machines

    @Test
    fun `a game in the AwaitingNextRound phase can only ever transition to Bidding`() =
        propertyTest {
            checkAll(
                propTestConfig,
                // TODO: I'm using gameWithValidPlayers as a shortcut, otherwise the test would take way too long to run.
                //  check this in the book.
                Arb.game.validOnly().filter { it.deprecatedState.state is AwaitingNextRound },
                Arb.command,
            ) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.deprecatedState.state).isA<Bidding>()
            }.printStatistics().checkCoveragePercentages(expectGameStates())
        }
}
