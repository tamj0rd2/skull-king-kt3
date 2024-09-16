package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.random
import java.util.UUID

@JvmInline
value class GameId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<GameId>(::GameId)
}

class Game {
    private var initialized = false

    private constructor(history: List<GameEvent>) {
        check(history.isNotEmpty()) { "Provided history was empty. Create a new game instead." }

        id = history.first().gameId
        check(history.all { it.gameId == this.id }) { "GameId mismatch" }

        history.forEach { event ->
            when (event) {
                is PlayerJoined -> addPlayer(event.playerId)
                is GameCreated -> Unit.asSuccess()
            }.orThrow()
        }

        initialized = true
    }

    private constructor() {
        id = GameId.random()
        initialized = true
        _updates.add(GameCreated(id))
    }

    val id: GameId

    private val _updates = mutableListOf<GameEvent>()
    val updates: List<GameEvent> get() = _updates.toList()

    private val _players = mutableListOf<PlayerId>()
    val players: List<PlayerId> get() = _players

    fun addPlayer(playerId: PlayerId): Result4k<Unit, AddPlayerErrorCode> {
        if (players.size >= MAXIMUM_PLAYER_COUNT) return GameIsFull.asFailure()
        _players.add(playerId)
        recordEvent(PlayerJoined(id, playerId))
        return Unit.asSuccess()
    }

    private fun recordEvent(event: GameEvent) {
        if (initialized) _updates.add(event)
    }

    companion object {
        const val MAXIMUM_PLAYER_COUNT = 6

        fun new() = Game()

        fun from(history: List<GameEvent>) = Game(history)
    }
}

sealed class GameErrorCode : RuntimeException()

sealed class AddPlayerErrorCode : GameErrorCode()

data object GameIsFull : AddPlayerErrorCode() {
    @Suppress("unused")
    private fun readResolve(): Any = GameIsFull
}
