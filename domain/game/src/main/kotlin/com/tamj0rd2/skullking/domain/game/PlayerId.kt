package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import java.util.UUID

// TODO: move this into the game package.
@JvmInline
value class PlayerId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<PlayerId>(::PlayerId, validation = { it != UUID(0, 0) }) {
        val NONE = PlayerId(UUID(0, 0))
    }
}
