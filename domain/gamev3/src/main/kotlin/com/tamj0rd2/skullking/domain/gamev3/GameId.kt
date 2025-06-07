package com.tamj0rd2.skullking.domain.gamev3

import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import java.util.UUID

sealed interface GameId

@JvmInline
value class SomeGameId private constructor(override val value: UUID) : GameId, Value<UUID> {
    companion object :
        UUIDValueFactory<SomeGameId>(::SomeGameId, validation = { it != UUID(0, 0) })
}

data object NoGameId : GameId
