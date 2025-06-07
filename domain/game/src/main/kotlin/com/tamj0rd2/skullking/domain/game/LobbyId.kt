package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.AggregateId
import dev.forkhandles.values.UUIDValueFactory
import java.util.UUID

@JvmInline
value class LobbyId private constructor(override val value: UUID) : AggregateId {
    companion object : UUIDValueFactory<LobbyId>(::LobbyId, validation = { it != UUID(0, 0) }) {
        val NONE = LobbyId(UUID(0, 0))
    }
}
