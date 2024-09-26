package com.tamj0rd2.skullking.domain.model

import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import java.util.UUID

@JvmInline
value class PlayerId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<PlayerId>(::PlayerId)
}

data class Player(
    val id: PlayerId,
)
