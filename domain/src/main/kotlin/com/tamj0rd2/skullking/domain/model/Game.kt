package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.map
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
    private constructor(providedHistory: List<GameEvent>) {
        check(providedHistory.isNotEmpty()) { "Provided history was empty. Create a new game instead." }

        id = providedHistory.first().gameId
        check(providedHistory.all { it.gameId == id }) { "GameId mismatch" }

        history = providedHistory.toList()
        history.forEach { event ->
            when (event) {
                is PlayerJoined -> addPlayer(event.playerId)
                is GameCreated -> Unit.asSuccess()
            }.orThrow()
        }
        recordUpdates = true
    }

    private constructor() {
        id = GameId.random()
        history = emptyList()
        recordUpdates = true
        appendEvent(GameCreated(id))
    }

    val id: GameId
    val history: List<GameEvent>
    private val _updates = mutableListOf<GameEvent>()
    val updates: List<GameEvent> get() = _updates.toList()

    private val state = GameState()
    val players: List<PlayerId> get() = state.players.toList()

    fun addPlayer(playerId: PlayerId): Result4k<Unit, AddPlayerErrorCode> {
        val event = PlayerJoined(id, playerId)
        return state.apply(event).map { appendEvent(event) }
    }

    private fun appendEvent(event: GameEvent) {
        state.apply(event)

        if (recordUpdates) _updates += event
    }

    private var recordUpdates = false

    companion object {
        const val MAXIMUM_PLAYER_COUNT = 6

        fun new() = Game()

        fun from(history: List<GameEvent>) = Game(history)
    }

    private class GameState {
        private val _players = mutableListOf<PlayerId>()
        val players: List<PlayerId> get() = _players.toList()

        fun apply(event: PlayerJoined): Result4k<Unit, AddPlayerErrorCode> {
            if (players.size >= MAXIMUM_PLAYER_COUNT) return GameIsFull.asFailure()
            if (players.contains(event.playerId)) return PlayerHasAlreadyJoined.asFailure()

            _players.add(event.playerId)
            return Unit.asSuccess()
        }

        fun apply(event: GameEvent): Result4k<Unit, AddPlayerErrorCode> =
            when (event) {
                is GameCreated -> Unit.asSuccess()
                is PlayerJoined -> apply(event)
            }
    }
}

sealed class GameErrorCode : RuntimeException()

sealed class AddPlayerErrorCode : GameErrorCode()

data object GameIsFull : AddPlayerErrorCode() {
    private fun readResolve(): Any = GameIsFull
}

data object PlayerHasAlreadyJoined : AddPlayerErrorCode() {
    private fun readResolve(): Any = PlayerHasAlreadyJoined
}
