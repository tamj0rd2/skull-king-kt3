package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.output.EventStore
import com.tamj0rd2.skullking.domain.game.Version

class EventStoreInMemoryAdapter<ID, Event : Any> : EventStore<ID, Event> {
    private val savedEvents = mutableMapOf<ID, List<Event>>()

    override fun append(
        entityId: ID,
        expectedVersion: Version,
        events: Collection<Event>,
    ) {
        val currentlySavedEvents = savedEvents.getOrDefault(entityId, emptyList())
        val currentlySavedVersion = currentlySavedEvents.version()

        if (expectedVersion == currentlySavedVersion) {
            savedEvents[entityId] = currentlySavedEvents + events
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

    companion object {
        private fun Collection<*>.version() = Version.of(size)
    }
}
