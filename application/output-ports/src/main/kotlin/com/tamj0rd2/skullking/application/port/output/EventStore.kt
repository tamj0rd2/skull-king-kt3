package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.Event
import com.tamj0rd2.skullking.domain.game.Version

interface EventStore<ID, E : Event<ID>> {
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

    fun subscribe(subscriber: EventStoreSubscriber<ID, E>)

    companion object {
        fun concurrentModificationException(
            expectedVersion: Version,
            actualVersion: Version,
        ) = ConcurrentModificationException(
            "Expected the most recent entity version to be $expectedVersion but the actual version was $actualVersion",
        )
    }
}

fun interface EventStoreSubscriber<out ID, in E : Event<ID>> {
    fun receive(events: Collection<E>)
}
