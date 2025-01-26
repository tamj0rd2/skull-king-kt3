package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import java.util.UUID

@JvmInline
value class LobbyId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<LobbyId>(::LobbyId, validation = { it != UUID(0, 0) }) {
        val NONE = LobbyId(UUID(0, 0))
    }
}
