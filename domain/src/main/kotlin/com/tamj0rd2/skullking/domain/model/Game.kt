package com.tamj0rd2.skullking.domain.model

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.random
import java.util.*

@JvmInline
value class GameId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<GameId>(::GameId)
}

@Suppress("CONTEXT_RECEIVERS_DEPRECATED") // When contextParameters are available, I'll migrate.
class Game(
    val id: GameId,
    history: List<GameEvent> = emptyList(),
) {
    private var initialized = false

    val changes: List<GameEvent>
        field = mutableListOf<GameEvent>()

    val players: List<PlayerId>
        field = mutableListOf<PlayerId>()

    init {
        // TODO: Throw a typed exception here. Or maybe even turn this into a factory somehow?
        check(history.all { it.gameId == id }) { "GameId mismatch" }

        history.forEach { event ->
            when (event) {
                is PlayerJoined -> addPlayer(event.playerId)
            }.orThrow()
        }
        initialized = true
    }

    fun addPlayer(playerId: PlayerId): Result4k<Unit, AddPlayerErrorCode> {
        if (players.size >= MAXIMUM_PLAYER_COUNT) return Failure(GameIsFull)
        players.add(playerId)
        recordEvent(PlayerJoined(id, playerId))
        return Success(Unit)
    }

    private fun recordEvent(event: GameEvent) {
        if (initialized) changes.add(event)
    }

    companion object {
        const val MAXIMUM_PLAYER_COUNT = 6

        fun new() = Game(GameId.random(), emptyList())
    }
}

sealed class GameErrorCode : RuntimeException()

sealed class AddPlayerErrorCode : GameErrorCode()

data object GameIsFull : AddPlayerErrorCode() {
    @Suppress("unused")
    private fun readResolve(): Any = GameIsFull
}
