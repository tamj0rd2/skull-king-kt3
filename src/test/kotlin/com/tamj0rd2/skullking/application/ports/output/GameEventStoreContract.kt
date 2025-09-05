package com.tamj0rd2.skullking.application.ports.output

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.isEmpty
import com.natpryce.hamkrest.throws
import com.tamj0rd2.skullking.domain.Version
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameEvent
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.testsupport.eventually
import dev.forkhandles.values.random
import java.util.*
import org.junit.jupiter.api.Test

interface GameEventStoreContract {
    val eventStore: GameEventStore

    @Test
    fun `events for an aggregate can be stored and retrieved`() {
        val game = Game.new(PlayerId.random())

        eventStore.append(game.events, Version.initial)

        assertThat(eventStore.read(game.id), !isEmpty and equalTo(game.events))
    }

    @Test
    fun `can subscribe to receive game events`() {
        val subscriber = SpyGameEventSubscriber()
        eventStore.subscribe(subscriber)

        val gameId = GameId.random()
        val eventsToAppend = listOf(GameEvent.GameCreated(gameId, PlayerId.random()))
        eventStore.append(eventsToAppend, Version.initial)

        eventually { assertThat(subscriber.receivedEvents.map { it.gameId }, hasElement(gameId)) }
    }

    @Test
    fun `can only append events for a single entity at a time`() {
        val gameId1 = GameId.random()
        val gameId2 = GameId.random()

        val eventsToAppend =
            listOf(GameEvent.GameCreated(gameId1, PlayerId.of("test-player")), GameEvent.GameCreated(gameId2, PlayerId.of("test-player")))

        assertThat({ eventStore.append(eventsToAppend, Version.initial) }, throws<CannotSaveEventsForMultipleGames>())
    }

    @Test
    fun `when optimistic concurrency fails, an error is thrown`() {
        val gameId = GameId.random()
        eventStore.append(listOf(GameEvent.GameCreated(gameId, PlayerId.random())), Version.initial)

        val eventThatBobWantsToAppend = GameEvent.PlayerJoined(gameId, PlayerId.of("bob"))
        val eventThatSammyWantsToAppend = GameEvent.PlayerJoined(gameId, PlayerId.of("sammy"))
        assertThat(eventThatBobWantsToAppend, !equalTo(eventThatSammyWantsToAppend))

        // this should work because there is no contention.
        eventStore.append(newEvents = listOf(eventThatBobWantsToAppend), expectedVersion = Version.of(1))

        // should fail because the latest event version has changed as a side effect of Bob
        // committing his changes.
        assertThat(
            { eventStore.append(newEvents = listOf(eventThatSammyWantsToAppend), expectedVersion = Version.of(1)) },
            throws<OptimisticLockingException>(),
        )

        // this should work because Sammy has now used the correct expected version. In practice,
        // the caller should
        // re-load the events/entity, and use the updated state to decide whether the command should
        // still be executed.
        eventStore.append(newEvents = listOf(eventThatSammyWantsToAppend), expectedVersion = Version.of(2))
    }

    @Test
    fun `read throws if there are no events for the given game id`() {
        val gameId = GameId.random()
        assertThat({ eventStore.read(gameId) }, throws<GameNotFoundException>())
    }
}

private fun PlayerId.Companion.random() = PlayerId.of("test-player-${UUID.randomUUID().toString().take(8)}")
