package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.GameActionArbs.gameActionsArb
import dev.forkhandles.values.random
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isLessThanOrEqualTo
import strikt.assertions.isNotEqualTo
import strikt.assertions.one
import strikt.assertions.size

// I'm ok with these tests taking longer to run if necessary because these invariants are important
@Timeout(10)
class GameTests {
    @Test
    fun `games always start with a GameCreated event`() =
        invariant { game ->
            expectThat(game.events).first().isA<GameCreatedEvent>()
        }

    @Test
    fun `games always have a single GameCreated event`() =
        invariant { game ->
            expectThat(game.events).one { isA<GameCreatedEvent>() }
        }

    @Test
    fun `all events within a game relate to that specific game`() =
        // specifying the iterations because 1000 iterations * 100 events is just too many to run the tests in a reasonable timeframe.
        invariant(iterations = 200) { game ->
            expectThat(game.events).all { get { gameId }.isEqualTo(game.id) }
        }

    @Test
    fun `games can never have more than 6 players`() =
        invariant { game ->
            expectThat(game.state.players).size.isLessThanOrEqualTo(MAXIMUM_PLAYER_COUNT)
        }

    @Test
    fun `the players in the game are always unique`() =
        invariant { game ->
            expectThat(game.state.players).doesNotContainAnyDuplicateValues()
        }

    @Test
    fun `the players in the game always have a non-zero id`() =
        invariant { game ->
            expectThat(game.state.players).all { isNotEqualTo(PlayerId.NONE) }
        }

    @Test
    fun `a game can be restored using its history of events`() =
        invariant { game ->
            val restoredGame = Game.from(game.events)
            expectThat(restoredGame) {
                get { events }.isEqualTo(game.events)
                get { state }.isEqualTo(game.state)
            }
        }

    // TODO: confusingly, an unsaved game starts at version -1. I don't like that. Make unsaved games 0 to make it easier to understand
    //  that would mean that the minimum version for a restored game would be 1 (if it was saved right after it was created.)
    @Test
    fun `a game that has been restored using a history of events has a loaded version corresponding to the number of events`() {
        invariant { game ->
            val restoredGame = Game.from(game.events)
            expectThat(restoredGame.loadedAtVersion).isEqualTo(Version.of(game.events.size - 1))
        }

        example {
            val game = Game.new(PlayerId.random())
            game.execute(GameAction.AddPlayer(PlayerId.random()))
            expectThat(game.newEventsSinceGameWasLoaded).hasSize(2) // the inherent game created event + the add player event.

            val restoredGame = Game.from(game.events)
            expectThat(restoredGame.loadedAtVersion).isEqualTo(Version.of(1))
        }
    }

    @Test
    fun `games that were not restored from a history of events don't have a loaded version`() {
        invariant { game ->
            expectThat(game.loadedAtVersion).isEqualTo(Version.NONE)
        }
    }

    // TODO: this seems like it should be an invariant of a Hand model.
    @Test
    @Disabled
    fun `within the players hands, there can't be more cards than exist of that type (new cards aren't invented from thin air)`() {
        TODO()
    }
}

internal fun invariant(
    iterations: Int = 1000,
    checkInvariant: (Game) -> Unit,
) = invariant(iterations) { game, action ->
    // I don't care whether the action succeeds.
    // I just want to ensure the invariants are always upheld regardless.
    game.execute(action)
    checkInvariant(game)
}

internal fun invariant(
    iterations: Int = 1000,
    arb: Arb<List<GameAction>> = gameActionsArb,
    checkInvariant: (Game, GameAction) -> Unit,
) = propertyTest {
    arb.checkAll(iterations) { gameActions ->
        val game = Game.new(PlayerId.random())
        gameActions.forEach { action ->
            checkInvariant(game, action)
        }
    }
}

internal fun example(block: () -> Unit) = block()

private fun <T> Assertion.Builder<List<T>>.doesNotContainAnyDuplicateValues() =
    apply {
        containsExactlyInAnyOrder(subject.toSet())
    }
