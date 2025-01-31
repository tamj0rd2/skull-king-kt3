package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.output.EventStore
import com.tamj0rd2.skullking.application.port.output.EventStoreSubscriber
import com.tamj0rd2.skullking.domain.game.Version

class EventStoreInMemoryAdapter<ID, Event : Any>(
    initialSubscribers: List<EventStoreSubscriber<Event>> = emptyList(),
) : EventStore<ID, Event> {
    private val savedEvents = mutableMapOf<ID, List<Event>>()
    private val subscribers = mutableListOf(*initialSubscribers.toTypedArray())

    override fun append(
        entityId: ID,
        expectedVersion: Version,
        events: Collection<Event>,
    ) {
        val currentlySavedEvents = savedEvents.getOrDefault(entityId, emptyList())
        val currentlySavedVersion = currentlySavedEvents.version()

        if (expectedVersion == currentlySavedVersion) {
            savedEvents[entityId] = currentlySavedEvents + events
            // NOTE: this opens up potential consistency issues. but maybe that's ok for an in memory fake 🤷
            subscribers.forEach { it.receive(events) }
            return
        }

        if (currentlySavedEvents.drop(expectedVersion.value) == events) {
            // someone is trying to make exactly the same writes again, so use idempotence and do nothing.
            return
        }

        throw ConcurrentModificationException(
            "Expected most recent entity version to be $expectedVersion but was $currentlySavedVersion",
        )
    }

    override fun read(entityId: ID): Collection<Event> = savedEvents[entityId] ?: emptyList()

    override fun subscribe(subscriber: EventStoreSubscriber<Event>) {
        subscribers.add(subscriber)
    }

    companion object {
        private fun Collection<*>.version() = Version.of(size)
    }
}
