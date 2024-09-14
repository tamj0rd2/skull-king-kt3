package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.skullking.port.output.GameEventsPort
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.onFailure
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
    private val id: GameId,
    history: List<GameEvent> = emptyList(),
) {
    private var initialized = false
    private val changes = mutableListOf<GameEvent>()
    private val _players = mutableListOf<PlayerId>()
    val players get() = _players.toList()

    init {
        history.forEach { event ->
            when (event) {
                is PlayerJoined -> addPlayer(event.playerId)
            }
        }
        initialized = true
    }

    fun addPlayer(playerId: PlayerId): Result4k<Unit, AddPlayerErrorCode> {
        if (players.size >= MAXIMUM_PLAYER_COUNT) return Failure(GameIsFull)
        _players.add(playerId)
        recordEvent(PlayerJoined(id, playerId))
        return Success(Unit)
    }

    private fun recordEvent(event: GameEvent) {
        if (initialized) changes.add(event)
    }

    companion object {
        const val MAXIMUM_PLAYER_COUNT = 6

        fun new() = Game(GameId.random(), emptyList())

        context(GameEventsPort)
        internal fun addPlayer(
            gameId: GameId,
            playerId: PlayerId,
        ): Result4k<Unit, AddPlayerErrorCode> {
            val game = Game(gameId, findGameEvents(gameId))
            game.addPlayer(playerId).onFailure { return it }
            saveGameEvents(game.changes)
            return Success(Unit)
        }
    }
}

sealed interface GameErrorCode

sealed interface AddPlayerErrorCode : GameErrorCode

data object GameIsFull : AddPlayerErrorCode
