package com.tamj0rd2.skullking.domain

import dev.forkhandles.values.Value
import java.util.UUID

interface Event<out ID : EntityId> {
    // TODO: rename this to aggregateId
    val entityId: ID
}

interface EntityId : Value<UUID>
