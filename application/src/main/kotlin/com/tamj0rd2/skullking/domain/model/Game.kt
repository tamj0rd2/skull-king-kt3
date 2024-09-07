package com.tamj0rd2.skullking.domain.model

import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import java.util.UUID

@JvmInline
value class GameId private constructor(override val value: UUID): Value<UUID> {
    companion object : UUIDValueFactory<GameId>(::GameId)
}

data class Game private constructor(
    val id: GameId,
    val players: List<PlayerId>
) {
    companion object {
        fun new(id: GameId): Game = Game(id, emptyList())
    }

    fun addPlayer(playerId: PlayerId): Game =
        copy(players = players + playerId)
}
