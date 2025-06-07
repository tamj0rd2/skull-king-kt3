package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.output.EventStore
import com.tamj0rd2.skullking.application.port.output.EventStoreSubscriber
import com.tamj0rd2.skullking.domain.AggregateId
import com.tamj0rd2.skullking.domain.Event
import com.tamj0rd2.skullking.domain.game.Version

class EventStoreInMemoryAdapter<ID : AggregateId, E : Event<ID>>(
    initialSubscribers: List<EventStoreSubscriber<ID, E>> = emptyList()
) : EventStore<ID, E> {
    private val savedEvents = mutableMapOf<ID, List<E>>()
    private val subscribers = mutableListOf(*initialSubscribers.toTypedArray())

    override fun append(entityId: ID, expectedVersion: Version, events: Collection<E>) {
        val currentlySavedEvents = savedEvents.getOrDefault(entityId, emptyList())
        val currentlySavedVersion = currentlySavedEvents.version()

        if (expectedVersion == currentlySavedVersion) {
            savedEvents[entityId] = currentlySavedEvents + events
            // NOTE: this opens up potential consistency issues. but maybe that's ok for an in
            // memory fake 🤷
            events.forEachIndexed { index, event ->
                subscribers.forEach { subscriber ->
                    subscriber.onEventReceived(entityId, currentlySavedVersion + index + 1)
                }
            }
            return
        }

        if (currentlySavedEvents.drop(expectedVersion.value) == events) {
            // someone is trying to make exactly the same writes again, so use idempotence and do
            // nothing.
            return
        }

        throw ConcurrentModificationException(
            "Expected most recent entity version to be $expectedVersion but was $currentlySavedVersion"
        )
    }

    override fun read(entityId: ID): Collection<E> = savedEvents[entityId] ?: emptyList()

    override fun subscribe(subscriber: EventStoreSubscriber<ID, E>) {
        subscribers.add(subscriber)
    }

    companion object {
        private fun Collection<*>.version() = Version.of(size)
    }
}
