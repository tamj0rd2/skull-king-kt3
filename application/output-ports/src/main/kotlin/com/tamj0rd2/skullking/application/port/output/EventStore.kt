package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.game.Version

interface EventStore<ID, Event : Any> {
    fun append(
        entityId: ID,
        expectedVersion: Version,
        events: Collection<Event>,
    )

    fun read(entityId: ID): Collection<Event>

    companion object {
        fun concurrentModificationException(
            expectedVersion: Version,
            actualVersion: Version,
        ) = ConcurrentModificationException(
            "Expected the most recent entity version to be $expectedVersion but the actual version was $actualVersion",
        )
    }
}
