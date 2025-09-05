package com.tamj0rd2.skullking.application.ports.output

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.isEmpty
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameEvent
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.testsupport.eventually
import dev.forkhandles.values.random
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

fun PlayerId.Companion.random() = PlayerId.of("test-player")

interface GameEventStoreContract {
    val eventStore: GameEventStore

    @Test
    fun `events for an aggregate can be stored and retrieved`() {
        val game = Game.new(PlayerId.random())

        eventStore.append(game.events)

        assertThat(eventStore.read(game.id), !isEmpty and equalTo(game.events))
    }

    @Test
    fun `can subscribe to receive game events`() {
        val subscriber = SpyGameEventSubscriber()
        eventStore.subscribe(subscriber)

        val gameId = GameId.random()
        val eventsToAppend = listOf(GameEvent.GameCreated(gameId, PlayerId.random()))
        eventStore.append(eventsToAppend)

        eventually { assertThat(subscriber.receivedEvents.map { it.gameId }, hasElement(gameId)) }
    }

    @Test
    @Disabled
    fun `can only append events for a single entity at a time`() {
        TODO()
    }

    //    @Test
    //    fun `when optimistic concurrency fails, an error is thrown`() {
    //        val gameId = GameId.random()
    //        eventStore.append(listOf(GameEvent.GameCreated(gameId, PlayerId.random())))
    //
    //        val eventThatBobWantsToAppend = GameEvent.PlayerJoined(gameId, PlayerId.random())
    //        val eventThatSammyWantsToAppend = GameEvent.PlayerJoined(gameId, PlayerId.random())
    //        assertThat(eventThatBobWantsToAppend, !equalTo(eventThatSammyWantsToAppend))
    //
    //        // this should work because there is no contention.
    //        eventStore.append(
    //            entityId = gameId,
    //            expectedVersion = Version.of(1),
    //            events = listOf(eventThatBobWantsToAppend),
    //        )
    //
    //        // should fail because the latest event version has changed as a side effect of Bob
    //        // committing his changes.
    //        expectThrows<ConcurrentModificationException> {
    //            eventStore.append(
    //                entityId = gameId,
    //                expectedVersion = Version.of(1),
    //                events = listOf(eventThatSammyWantsToAppend),
    //            )
    //        }
    //
    //        // this should work because Sammy has now used the correct expected version. In practice,
    //        // the caller should
    //        // re-load the events/entity, and use the updated state to decide whether the command should
    //        // still be executed.
    //        eventStore.append(
    //            entityId = gameId,
    //            expectedVersion = Version.of(2),
    //            events = listOf(eventThatSammyWantsToAppend),
    //        )
    //    }
}
