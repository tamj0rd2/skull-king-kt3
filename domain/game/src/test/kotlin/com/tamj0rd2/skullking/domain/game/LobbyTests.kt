package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.Lobby.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.LobbyActionArbs.lobbyCommandsArb
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
class LobbyTests {
    @Test
    fun `lobbies always start with a LobbyCreated event`() =
        invariant { lobby ->
            expectThat(lobby.allEvents).first().isA<LobbyCreatedEvent>()
        }

    @Test
    fun `lobbies always have a single LobbyCreated event`() =
        invariant { lobby ->
            expectThat(lobby.allEvents).one { isA<LobbyCreatedEvent>() }
        }

    @Test
    fun `all events within a lobby relate to that specific lobby`() =
        // specifying the iterations because 1000 iterations * 100 events is just too many to run the tests in a reasonable timeframe.
        invariant(iterations = 200) { lobby ->
            expectThat(lobby.allEvents).all { get { lobbyId }.isEqualTo(lobby.id) }
        }

    @Test
    fun `lobbies can never have more than 6 players`() =
        invariant { lobby ->
            expectThat(lobby.state.players).size.isLessThanOrEqualTo(MAXIMUM_PLAYER_COUNT)
        }

    @Test
    fun `the players in the lobby are always unique`() =
        invariant { lobby ->
            expectThat(lobby.state.players).doesNotContainAnyDuplicateValues()
        }

    @Test
    fun `the players in the lobby always have a non-zero id`() =
        invariant { lobby ->
            expectThat(lobby.state.players).all { isNotEqualTo(PlayerId.NONE) }
        }

    @Test
    fun `a lobby can be restored using its history of events`() =
        invariant { lobby ->
            val restoredLobby = Lobby.from(lobby.allEvents)
            expectThat(restoredLobby) {
                get { allEvents }.isEqualTo(lobby.allEvents)
                get { state }.isEqualTo(lobby.state)
            }
        }

    @Test
    fun `a lobby that has been restored using a history of events has a loaded version corresponding to the number of events`() {
        example {
            val lobby = Lobby.new(PlayerId.random())
            lobby.execute(LobbyCommand.AddPlayer(PlayerId.random()))
            expectThat(lobby.newEventsSinceLobbyWasLoaded).hasSize(2) // the inherent game created event + the add player event.

            val restoredLobby = Lobby.from(lobby.allEvents)
            expectThat(restoredLobby.loadedAtVersion).isEqualTo(Version.of(2))
        }

        invariant { lobby ->
            val restoredLobby = Lobby.from(lobby.allEvents)
            expectThat(restoredLobby.loadedAtVersion).isEqualTo(Version.of(lobby.allEvents.size))
        }
    }

    @Test
    fun `lobbies that were not restored from a history of events don't have a loaded version`() {
        invariant { lobby ->
            expectThat(lobby.loadedAtVersion).isEqualTo(Version.NONE)
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
    checkInvariant: (Lobby) -> Unit,
) = invariant(iterations) { lobby, action ->
    // I don't care whether the action succeeds.
    // I just want to ensure the invariants are always upheld regardless.
    lobby.execute(action)
    checkInvariant(lobby)
}

internal fun invariant(
    iterations: Int = 1000,
    arb: Arb<List<LobbyCommand>> = lobbyCommandsArb,
    checkInvariant: (Lobby, LobbyCommand) -> Unit,
) = propertyTest {
    arb.checkAll(iterations) { lobbyCommands ->
        val lobby = Lobby.new(PlayerId.random())
        lobbyCommands.forEach { action ->
            checkInvariant(lobby, action)
        }
    }
}

internal fun example(block: () -> Unit) = block()

private fun <T> Assertion.Builder<List<T>>.doesNotContainAnyDuplicateValues() =
    apply {
        containsExactlyInAnyOrder(subject.toSet())
    }
