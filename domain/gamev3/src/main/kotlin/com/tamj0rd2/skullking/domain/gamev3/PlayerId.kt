package com.tamj0rd2.skullking.domain.gamev3

import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import java.util.UUID

sealed interface PlayerId

@JvmInline
value class SomePlayerId private constructor(override val value: UUID) : PlayerId, Value<UUID> {
    companion object :
        UUIDValueFactory<SomePlayerId>(::SomePlayerId, validation = { it != UUID(0, 0) })
}

data object NoPlayerId : PlayerId
