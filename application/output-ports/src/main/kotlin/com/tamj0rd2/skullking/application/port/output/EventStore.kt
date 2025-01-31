package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.game.Version

interface EventStore<ID, Event : Any> {
    fun append(
        entityId: ID,
        expectedVersion: Version,
        events: Collection<Event>,
    )

    fun read(entityId: ID): Collection<Event>
}
