package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.game.Lobby
import com.tamj0rd2.skullking.domain.game.LobbyCreatedEvent
import com.tamj0rd2.skullking.domain.game.LobbyEvent
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.domain.game.PlayerJoinedEvent
import com.tamj0rd2.skullking.domain.game.Version
import dev.forkhandles.values.random
import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure
import strikt.assertions.isNotEqualTo
import strikt.assertions.isSuccess

interface EventStoreContract {
    val eventStore: EventStore<LobbyId, LobbyEvent>

    @Test
    fun `events for an entity can be stored and retrieved`() {
        val lobby = Lobby.new(PlayerId.random())

        eventStore.append(lobby.id, Version.NONE, lobby.newEventsSinceLobbyWasLoaded)

        expectThat(eventStore.read(lobby.id)).isEqualTo(lobby.newEventsSinceLobbyWasLoaded)
    }

    @Test
    fun `when optimistic concurrency fails, an error is thrown`() {
        val lobbyId = LobbyId.random()
        eventStore.append(lobbyId, Version.NONE, listOf(LobbyCreatedEvent(lobbyId, PlayerId.random())))

        val eventThatBobWantsToAppend = PlayerJoinedEvent(lobbyId, PlayerId.random())
        val eventThatSammyWantsToAppend = PlayerJoinedEvent(lobbyId, PlayerId.random())
        expectThat(eventThatBobWantsToAppend).isNotEqualTo(eventThatSammyWantsToAppend)

        // this should work because there is no contention.
        expectCatching {
            eventStore.append(
                entityId = lobbyId,
                expectedVersion = Version.of(1),
                events = listOf(eventThatBobWantsToAppend),
            )
        }.isSuccess()

        // should fail because the latest event version has changed as a side effect of Bob committing his changes.
        expectCatching {
            eventStore.append(
                entityId = lobbyId,
                expectedVersion = Version.of(1),
                events = listOf(eventThatSammyWantsToAppend),
            )
        }.isFailure().isA<ConcurrentModificationException>()

        // this should work because Sammy has now used the correct expected version. In practice, the caller should
        // re-load the events/entity, and use the updated state to decide whether the action can still be performed.
        expectCatching {
            eventStore.append(
                entityId = lobbyId,
                expectedVersion = Version.of(2),
                events = listOf(eventThatSammyWantsToAppend),
            )
        }.isSuccess()
    }

    // TODO: I should make sure that every event has an actor, just to be triply sure things are ok.
    @Test
    fun `writes are idempotent if trying to make exactly the same change for exactly the same version`() {
        val lobbyId = LobbyId.random()
        eventStore.append(lobbyId, Version.NONE, listOf(LobbyCreatedEvent(lobbyId, PlayerId.random())))

        val eventsToAppend = listOf(PlayerJoinedEvent(lobbyId, PlayerId.random()))

        // this should work because there is no contention.
        expectCatching {
            eventStore.append(
                entityId = lobbyId,
                expectedVersion = Version.of(1),
                events = eventsToAppend,
            )
        }.isSuccess()

        // should work because the version and events are identical
        expectCatching {
            eventStore.append(
                entityId = lobbyId,
                expectedVersion = Version.of(1),
                events = eventsToAppend,
            )
        }.isSuccess()
    }
}
