package com.tamj0rd2.skullking.domain.model.auth

import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import java.util.UUID

@JvmInline
value class SessionId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<SessionId>(::SessionId, validation = { it != UUID(0, 0) })
}
