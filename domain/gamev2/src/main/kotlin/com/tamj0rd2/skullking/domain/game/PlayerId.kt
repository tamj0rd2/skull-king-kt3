package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import java.util.UUID

data class PlayerId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<PlayerId>(::PlayerId)
}
