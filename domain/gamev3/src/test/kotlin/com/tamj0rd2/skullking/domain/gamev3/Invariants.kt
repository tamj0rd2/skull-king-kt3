package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.skullking.domain.gamev3.GameArbs.andACommand
import com.tamj0rd2.skullking.domain.gamev3.GameArbs.command
import com.tamj0rd2.skullking.domain.gamev3.GameArbs.game
import com.tamj0rd2.skullking.domain.gamev3.GameArbs.validOnly
import com.tamj0rd2.skullking.domain.gamev3.GameState.AwaitingNextRound
import com.tamj0rd2.skullking.domain.gamev3.GameState.Bidding
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
        fun GameStateName.isExpectedToBeTransitionable() =
            when (this) {
                GameStateName.TrickTaking -> false // TODO: remove this to drive further implementation
                else -> true
            }

        fun PropertyContext.checkCoverageForAllGameStates() =
            apply {
                checkCoverageExists(
                    "state",
                    GameStateName.entries
                        .minus(GameStateName.NotStarted)
                        .filter { it.isExpectedToBeTransitionable() }
                        .toSet(),
                )
            }

        fun PropertyContext.collectState(state: GameState) = collect("state", state.name)

        fun PropertyContext.collectState(game: Game) = collectState(game.state)

        fun PropertyContext.collectCommand(command: GameCommand) = collect("command", command::class.simpleName)

        fun PropertyContext.checkCoverageForCommands() =
            collect(GameCommand::class.sealedSubclasses.filter { it.isFinal }.map { it.simpleName })
    }

    @Test
    fun `a game in progress always has 2-6 players`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                assumeThat(game.state is GameState.InProgress)
                collectState(game)
                expectThat(game.state.players.size).isIn(2..6)
            }
        }.checkCoverageForAllGameStates()
    }

    @Test
    fun `a game always start with a GameStarted event`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collectState(game)
                expectThat(game.events.first()).isA<GameStartedEvent>()
            }
        }.checkCoverageForAllGameStates()
    }

    @Test
    fun `a game only ever has 1 GameStarted event`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collectState(game)
                expectThat(game.events).filterIsInstance<GameStartedEvent>().count().isEqualTo(1)
            }
        }.checkCoverageForAllGameStates()
    }

    @Test
    fun `each event in a game is related to that specific game`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collectState(game)
                collect("events", game.events.map { it::class.simpleName })
                expectThat(game.events).all { get { id }.isEqualTo(game.id) }
            }
        }.checkCoverageForAllGameStates()
    }

    @Test
    fun `each successful command results in 1 new event being emitted`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.events.size).isEqualTo(initialGame.events.size + 1)
            }
        }.checkCoverageForAllGameStates()
    }

    @Test
    fun `each successful command results in a state change`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.state).isNotEqualTo(initialGame.state)
            }
        }.checkCoverageForAllGameStates()
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
        }.checkCoverageForAllGameStates()
    }

    @Test
    fun `successful commands never change the game's ID`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.id).isEqualTo(initialGame.id)
            }
        }.checkCoverageForAllGameStates()
    }

    @Test
    fun `successful commands never cause existing events to be changed or removed`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame)
                expectThat(updatedGame.events.dropLast(1)).containsExactly(initialGame.events)
            }
        }.checkCoverageForAllGameStates()
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
        }.checkCoverageForAllGameStates()
    }

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

    @Test
    fun `a game in the Bidding state can only ever transition to TrickTaking`() {
        propertyTest {
            checkAll(
                propTestConfig,
                // TODO: can we filter while the arbs are being generated rather than after? might need to write my own arb
                Arb.game
                    .validOnly()
                    .filter { it.state is Bidding }
                    .andACommand,
            ) { (initialGame, command) ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectCommand(command)
                expectThat(updatedGame.state.name).isContainedIn(setOf(GameStateName.Bidding, GameStateName.TrickTaking))
            }
        }
    }

    @Test
    fun `successful commands cause the game to transition to the correct state`() {
        class TestOracle(
            startingStateName: GameStateName,
        ) {
            var currentStateName = startingStateName
                private set

            fun execute(command: GameCommand): TestOracle =
                apply {
                    if (!currentStateName.isExpectedToBeTransitionable()) return@apply

                    when (command) {
                        is StartRoundCommand,
                        -> if (currentStateName == GameStateName.AwaitingNextRound) currentStateName = GameStateName.Bidding

                        is PlaceBidCommand -> Unit
                        is StartTrickCommand -> if (currentStateName == GameStateName.Bidding) currentStateName = GameStateName.TrickTaking
                    }
                }
        }

        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val initialStateName = initialGame.state.name
                val testOracle = TestOracle(initialStateName).execute(command)

                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectState(initialGame.state)
                collectCommand(command)

                expectThat(updatedGame.state.name).isEqualTo(testOracle.currentStateName)
            }.checkCoverageForAllGameStates()
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
        }.checkCoverageForAllGameStates()
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
        }.checkCoverageForAllGameStates()
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
    fun `the number of bids placed in the game never exceeds the player count multiplied by 10`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game.validOnly()) { game ->
                collectState(game)
                val initialPlayers = (game.events.first() as GameStartedEvent).players
                expectThat(game.events.count { it is BidPlacedEvent }).isLessThanOrEqualTo(initialPlayers.size * 10)
            }.checkCoverageForAllGameStates()
        }
    }
}
