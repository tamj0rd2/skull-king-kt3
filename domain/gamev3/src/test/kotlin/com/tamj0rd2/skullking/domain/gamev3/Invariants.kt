package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.skullking.domain.gamev3.GameArbs.command
import com.tamj0rd2.skullking.domain.gamev3.GameArbs.game
import com.tamj0rd2.skullking.domain.gamev3.GameArbs.validOnly
import com.tamj0rd2.skullking.domain.gamev3.GameState.AwaitingNextRound
import com.tamj0rd2.skullking.domain.gamev3.GameState.Bidding
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.assumeThat
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
import strikt.assertions.all
import strikt.assertions.containsExactly
import strikt.assertions.count
import strikt.assertions.filterIsInstance
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isIn
import strikt.assertions.isNotEqualTo

class Invariants {
    private companion object {
        @Suppress("NO_REFLECTION_IN_CLASS_PATH")
        val inProgressGameStates =
            GameState.InProgress::class
                .sealedSubclasses
                .filter {
                    when (it) {
                        // TODO: Bidding state is wip. remove this line to help drive the behaviour.
                        Bidding::class -> false
                        else -> true
                    }
                }.map { it.simpleName }
                .toSet()
                .also { check(it.isNotEmpty()) }

        fun expectInProgressGameStates(): Map<Any?, Double> = inProgressGameStates.associateWith { 95.00 / inProgressGameStates.size }

        fun PropertyContext.collectState(game: Game) = collectState(game.state)

        fun PropertyContext.collectState(state: GameState) = collect(state::class.simpleName)
    }

    @Test
    fun `a game in progress always has 2-6 players`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                assumeThat(game.state is GameState.InProgress)
                collectState(game)
                expectThat(game.state.players.size).isIn(2..6)
            }.printStatistics().checkCoveragePercentages(expectInProgressGameStates())
        }

    @Test
    fun `a game always start with a GameStarted event`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collectState(game)
                expectThat(game.events.first()).isA<GameStartedEvent>()
            }.printStatistics().checkCoveragePercentages(expectInProgressGameStates())
        }

    @Test
    fun `a game only ever has 1 GameStarted event`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collectState(game)
                expectThat(game.events).filterIsInstance<GameStartedEvent>().count().isEqualTo(1)
            }.printStatistics().checkCoveragePercentages(expectInProgressGameStates())
        }

    @Test
    fun `each event in a game is related to that specific game`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collect(game.events.map { it::class.simpleName })
                expectThat(game.events).all { get { id }.isEqualTo(game.id) }
            }.printStatistics().checkCoveragePercentages(expectInProgressGameStates())
        }

    @Test
    fun `each successful command results in 1 new event being emitted`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.events.size).isEqualTo(initialGame.events.size + 1)
            }.printStatistics().checkCoveragePercentages(expectInProgressGameStates())
        }

    @Test
    fun `each successful command results in a state change`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.state).isNotEqualTo(initialGame.state)
            }.printStatistics().checkCoveragePercentages(expectInProgressGameStates())
        }

    @Test
    fun `successful commands never change the players in the game`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                // TODO: when I introduce a completed state, I should also check this here.
                assumeThat(initialGame.state is GameState.InProgress)

                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.state).isA<GameState.InProgress>().get { players }.isEqualTo(initialGame.state.players)
            }.printStatistics().checkCoveragePercentages(expectInProgressGameStates())
        }

    @Test
    fun `successful commands never change the game's ID`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.id).isEqualTo(initialGame.id)
            }.printStatistics().checkCoveragePercentages(expectInProgressGameStates())
        }

    @Test
    fun `successful commands never cause existing events to be changed or removed`() =
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.events.dropLast(1)).containsExactly(initialGame.events)
            }.printStatistics().checkCoveragePercentages(expectInProgressGameStates())
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
                    get { state }.isEqualTo(originalGame.state)
                }
            }.printStatistics().checkCoveragePercentages(expectInProgressGameStates())
        }

    // TODO: possibly put these in another file? Also double check my tests against what the book suggests for state machines

    @Test
    fun `a game in the AwaitingNextRound phase can only ever transition to Bidding`() =
        propertyTest {
            checkAll(
                propTestConfig,
                // TODO: I'm using gameWithValidPlayers as a shortcut, otherwise the test would take way too long to run.
                //  check this in the book.
                Arb.game.validOnly().filter { it.state is AwaitingNextRound },
                Arb.command,
            ) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.state).isA<Bidding>()
            }.printStatistics().checkCoveragePercentages(expectInProgressGameStates())
        }

    @Test
    fun `the round number of a game in progress never decreases`() =
        propertyTest {
            checkAll(
                propTestConfig,
                // TODO: I'm using gameWithValidPlayers as a shortcut, otherwise the test would take way too long to run.
                //  check this in the book.
                Arb.game.validOnly().filter { it.state is GameState.InProgress },
                Arb.command,
            ) { initialGame, command ->
                val initialState = initialGame.state as GameState.InProgress
                val updatedGameState = initialGame.execute(command).assumeWasSuccessful().state
                assumeThat(updatedGameState is GameState.InProgress)

                collectState(initialState)
                expectThat(updatedGameState.roundNumber).isGreaterThanOrEqualTo(initialState.roundNumber)
            }.printStatistics().checkCoveragePercentages(expectInProgressGameStates())
        }

    @Test
    fun `the round number of a game in progress only ever increases by 1 at most`() =
        propertyTest {
            checkAll(
                propTestConfig,
                // TODO: I'm using gameWithValidPlayers as a shortcut, otherwise the test would take way too long to run.
                //  check this in the book.
                Arb.game.validOnly().filter { it.state is GameState.InProgress },
                Arb.command,
            ) { initialGame, command ->
                val initialState = initialGame.state as GameState.InProgress
                val updatedGameState = initialGame.execute(command).assumeWasSuccessful().state
                assumeThat(updatedGameState is GameState.InProgress)

                collectState(initialState)
                expectThat(updatedGameState.roundNumber).isIn(initialState.roundNumber..initialState.roundNumber.next())
            }.printStatistics().checkCoveragePercentages(expectInProgressGameStates())
        }
}
