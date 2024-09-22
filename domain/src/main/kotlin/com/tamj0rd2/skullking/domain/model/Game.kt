package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.extensions.filterOrThrow
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
    companion object : UUIDValueFactory<GameId>(::GameId, validation = { it != UUID(0, 0) }) {
        val NONE = GameId(UUID(0, 0))
    }
}

class Game {
    private constructor() {
        id = GameId.random()
        history = emptyList()
        recordUpdates = true
        appendEvent(GameCreated(id))
    }

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

    val id: GameId
    var state = GameState.new()
        private set

    val history: List<GameEvent>
    private val _updates = mutableListOf<GameEvent>()
    val updates: List<GameEvent> get() = _updates.toList()

    fun addPlayer(playerId: PlayerId): Result4k<Unit, AddPlayerErrorCode> =
        appendEvent(PlayerJoined(id, playerId))
            .filterOrThrow<Unit, GameErrorCode, AddPlayerErrorCode>()

    private var recordUpdates = false

    private fun appendEvent(event: GameEvent): Result4k<Unit, GameErrorCode> =
        state.apply(event).map { nextState ->
            state = nextState
            if (recordUpdates) _updates += event
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
    private fun readResolve(): Any = GameIsFull
}

data object PlayerHasAlreadyJoined : AddPlayerErrorCode() {
    private fun readResolve(): Any = PlayerHasAlreadyJoined
}
