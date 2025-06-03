package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.skullking.domain.gamev3.GameArbs.command
import com.tamj0rd2.skullking.domain.gamev3.GameArbs.game
import com.tamj0rd2.skullking.domain.gamev3.GameArbs.validOnly
import com.tamj0rd2.skullking.domain.gamev3.GameState.AwaitingNextRound
import com.tamj0rd2.skullking.domain.gamev3.GameState.Bidding
import com.tamj0rd2.skullking.domain.gamev3.GameState.TrickTaking
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.assumeThat
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.assumeWasSuccessful
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.checkCoverageExists
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.printStatistics
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.propTestConfig
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.propertyTest
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.PropertyContext
import io.kotest.property.arbitrary.filter
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.containsExactly
import strikt.assertions.count
import strikt.assertions.filterIsInstance
import strikt.assertions.isA
import strikt.assertions.isContainedIn
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isIn
import strikt.assertions.isLessThanOrEqualTo
import strikt.assertions.isNotEqualTo

class Invariants {
    private companion object {
        @Suppress("NO_REFLECTION_IN_CLASS_PATH")
        val inProgressGameStates =
            GameState.InProgress::class
                .sealedSubclasses
                .minus(TrickTaking::class) // TODO: remove this to drive further implementation
                .map { it.simpleName }
                .toSet()
                .also { check(it.isNotEmpty()) }

        fun PropertyContext.checkCoverageForInProgressGameStates() = apply { checkCoverageExists("state", inProgressGameStates) }

        fun PropertyContext.collectState(state: GameState) = collect("state", state::class.simpleName)

        fun PropertyContext.collectState(game: Game) = collectState(game.state)
    }

    @Test
    fun `a game in progress always has 2-6 players`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                assumeThat(game.state is GameState.InProgress)
                collectState(game)
                expectThat(game.state.players.size).isIn(2..6)
            }
        }.checkCoverageForInProgressGameStates()
    }

    @Test
    fun `a game always start with a GameStarted event`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collectState(game)
                expectThat(game.events.first()).isA<GameStartedEvent>()
            }
        }.checkCoverageForInProgressGameStates()
    }

    @Test
    fun `a game only ever has 1 GameStarted event`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collectState(game)
                expectThat(game.events).filterIsInstance<GameStartedEvent>().count().isEqualTo(1)
            }
        }.checkCoverageForInProgressGameStates()
    }

    @Test
    fun `each event in a game is related to that specific game`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collectState(game)
                collect("events", game.events.map { it::class.simpleName })
                expectThat(game.events).all { get { id }.isEqualTo(game.id) }
            }
        }.checkCoverageForInProgressGameStates()
    }

    @Test
    fun `each successful command results in 1 new event being emitted`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.events.size).isEqualTo(initialGame.events.size + 1)
            }
        }.checkCoverageForInProgressGameStates()
    }

    @Test
    fun `each successful command results in a state change`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.state).isNotEqualTo(initialGame.state)
            }
        }.checkCoverageForInProgressGameStates()
    }

    @Test
    fun `successful commands never change the players in the game`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                // TODO: when I introduce a completed state, I should also check this here.
                assumeThat(initialGame.state is GameState.InProgress)

                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.state).isA<GameState.InProgress>().get { players }.isEqualTo(initialGame.state.players)
            }
        }.checkCoverageForInProgressGameStates()
    }

    @Test
    fun `successful commands never change the game's ID`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.id).isEqualTo(initialGame.id)
            }
        }.checkCoverageForInProgressGameStates()
    }

    @Test
    fun `successful commands never cause existing events to be changed or removed`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.events.dropLast(1)).containsExactly(initialGame.events)
            }
        }.checkCoverageForInProgressGameStates()
    }

    @Test
    fun `a game reconstituted from events has the same identity, state and events as the game it was reconstituted from`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { originalGame ->
                collectState(originalGame)
                expectThat(Game.reconstitute(originalGame.events).orThrow()) {
                    isEqualTo(originalGame)
                    get { id }.isEqualTo(originalGame.id)
                    get { events }.isEqualTo(originalGame.events)
                    get { state }.isEqualTo(originalGame.state)
                }
            }
        }.checkCoverageForInProgressGameStates()
    }

    // TODO: double check my tests against what the book suggests for state machines

    @Test
    fun `a game in the AwaitingNextRound state can only ever transition to Bidding`() {
        propertyTest {
            checkAll(
                propTestConfig,
                Arb.game.validOnly().filter { it.state is AwaitingNextRound },
                Arb.command,
            ) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                expectThat(updatedGame.state).isA<Bidding>()
            }.printStatistics()
        }
    }

    /*
    TODO: I think this is where I want to start using a test oracle to check state changes. I can write a super simple implementation that
        describes the intended state changes, without any of the validation logic. Then I can simply check that if the state in my production
        model has changed, it has followed the same state change as my test oracle.
     */
    @Test
    @Disabled("TODO: takes a lifetime to run")
    fun `a game in the Bidding state can only ever transition to TrickTaking`() {
        propertyTest {
            checkAll(
                propTestConfig,
                Arb.game.validOnly().filter { it.state is Bidding },
                Arb.command,
            ) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collect(command::class.simpleName)
                expectThat(updatedGame.state).isA<TrickTaking>()
            }.printStatistics()
        }
    }

    @Test
    fun `the round number of a game in progress never decreases`() {
        propertyTest {
            checkAll(
                propTestConfig,
                Arb.game.validOnly().filter { it.state is GameState.InProgress },
                Arb.command,
            ) { initialGame, command ->
                val initialState = initialGame.state as GameState.InProgress
                val updatedGameState = initialGame.execute(command).assumeWasSuccessful().state
                assumeThat(updatedGameState is GameState.InProgress)
                collectState(initialState)
                collect("command", command::class.simpleName)

                expectThat(updatedGameState.roundNumber).isGreaterThanOrEqualTo(initialState.roundNumber)
            }
        }.checkCoverageForInProgressGameStates()
    }

    @Test
    fun `the round number of a game in progress only ever increases by 1 at most`() {
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
            }
        }.checkCoverageForInProgressGameStates()
    }

    @Test
    fun `bids only include those by players in the game`() {
        propertyTest {
            checkAll(
                propTestConfig,
                Arb.game.validOnly().filter { it.state is Bidding },
            ) { game ->
                val state = game.state as Bidding
                expectThat(state.bids.keys).all { isContainedIn(state.players) }
                expectThat(game.events.filterIsInstance<BidPlacedEvent>()).all { get { playerId }.isContainedIn(state.players) }
            }
        }
    }

    @Test
    fun `the number of bids placed placed in the game never exceeds the player count multiplied by 10`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collectState(game)
                val initialPlayers = (game.events.first() as GameStartedEvent).players
                expectThat(game.events.count { it is BidPlacedEvent }).isLessThanOrEqualTo(initialPlayers.size * 10)
            }.checkCoverageForInProgressGameStates()
        }
    }
}
