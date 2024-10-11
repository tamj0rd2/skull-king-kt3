package com.tamj0rd2.skullking.domain.model.game

import com.tamj0rd2.skullking.domain.GameArbs.gameActionsArb
import com.tamj0rd2.skullking.domain.GameArbs.gameArb
import com.tamj0rd2.skullking.domain.model.game.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.game.Game.Companion.MINIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.propertyTest
import dev.forkhandles.result4k.orThrow
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.next
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isLessThanOrEqualTo
import strikt.assertions.one
import strikt.assertions.size

// I'm ok with these tests taking longer to run if necessary because these invariants are important
@Timeout(10)
class GameTests {
    private fun gameInvariant(
        iterations: Int = 1000,
        checkInvariant: (Game) -> Unit,
    ) = propertyTest {
        checkAll(iterations, gameActionsArb) { gameActions ->
            val game = Game.new()
            gameActions.applyEach { action ->
                // I don't care whether the action is actually possible.
                // I just want to ensure the invariants are always upheld.
                runCatching { action.mutate(game) }
                checkInvariant(game)
            }
        }
    }

    @Test
    fun `games always start with a GameCreated event`() =
        gameInvariant { game ->
            expectThat(game.events).first().isA<GameCreatedEvent>()
        }

    @Test
    fun `games always have a single GameCreated event`() =
        gameInvariant { game ->
            expectThat(game.events).one { isA<GameCreatedEvent>() }
        }

    @Test
    fun `a game can be restored using its history of events`() =
        gameInvariant { game ->
            val restoredGame = Game.from(game.events)
            expectThat(restoredGame) {
                get { events }.isEqualTo(game.events)
                get { state }.isEqualTo(game.state)
            }
        }

    @Test
    fun `all events within a game relate to that specific game`() =
        // specifying the iterations because 1000 iterations * 100 events is just too many to run the tests in a reasonable timeframe.
        gameInvariant(iterations = 200) { game ->
            expectThat(game.events).all { get { gameId }.isEqualTo(game.id) }
        }

    @Test
    fun `games can never have more than 6 players`() =
        gameInvariant { game ->
            expectThat(game.state.players).size.isLessThanOrEqualTo(MAXIMUM_PLAYER_COUNT)
        }

    @Test
    fun `the players in the game are always unique`() =
        gameInvariant { game ->
            expectThat(game.state.players).doesNotContainAnyDuplicateValues()
        }

    // TODO: this seems like it should be an invariant of a Hand model.
    @Test
    @Disabled
    fun `within the players hands, there can't be more cards than exist of that type (new cards aren't invented from thin air)`() {
        TODO()
    }

    @Nested
    inner class StartGameTests {
        // TODO: delete once covered in use case
        @Test
        fun `when the game is started, each player is dealt 1 card`() {
            val game = gameArb.filter { it.state.players.size >= MINIMUM_PLAYER_COUNT }.next()
            val players = game.state.players

            game.start().orThrow()

            expectThat(game.state.playerHands.keys).containsExactly(players)
            expectThat(game.state.playerHands.values).all { hasSize(1) }
        }
    }
}

private fun <T> Assertion.Builder<List<T>>.doesNotContainAnyDuplicateValues() =
    apply {
        containsExactlyInAnyOrder(subject.toSet())
    }
