package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.game.Version

interface EventStore<ID, E : Any> {
    fun append(
        entityId: ID,
        expectedVersion: Version,
        events: Collection<E>,
    )

    fun read(entityId: ID): Collection<E>

    fun read(
        entityId: ID,
        upToAndIncludingVersion: Version,
    ): Collection<E> = read(entityId).take(upToAndIncludingVersion.value)

    fun subscribe(subscriber: EventStoreSubscriber<E>)

    companion object {
        fun concurrentModificationException(
            expectedVersion: Version,
            actualVersion: Version,
        ) = ConcurrentModificationException(
            "Expected the most recent entity version to be $expectedVersion but the actual version was $actualVersion",
        )
    }
}

fun interface EventStoreSubscriber<E : Any> {
    fun receive(events: Collection<E>)
}
