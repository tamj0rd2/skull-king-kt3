package com.tamj0rd2.skullking.domain

import dev.forkhandles.values.Value
import java.util.UUID

interface Event<out ID : AggregateId> {
    val aggregateId: ID
}

interface AggregateId : Value<UUID>
