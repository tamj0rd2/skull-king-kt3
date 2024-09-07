package com.tamj0rd2.skullking.domain.model

import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import java.util.UUID

@JvmInline
value class GameId private constructor(override val value: UUID): Value<UUID> {
    companion object : UUIDValueFactory<GameId>(::GameId)
}

sealed interface GameEvent {
    val gameId: GameId
}

data class PlayerJoined(
    override val gameId: GameId,
    val playerId: PlayerId,
): GameEvent
