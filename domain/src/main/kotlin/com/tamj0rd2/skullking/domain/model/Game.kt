package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.extensions.filterOrThrow
import com.tamj0rd2.utils.pretty
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.minValue
import dev.forkhandles.values.random
import java.util.UUID

@JvmInline
value class GameId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<GameId>(::GameId, validation = { it != UUID(0, 0) })
}

@JvmInline
value class Version private constructor(
    override val value: Int,
) : Value<Int> {
    companion object : IntValueFactory<Version>(::Version, 0.minValue) {
        val NONE = Version(-1)
    }
}

class Game {
    override fun toString(): String = "${super.toString()}\n${state.pretty()}"

    private constructor() {
        id = GameId.random()
        appendEvent(GameCreated(id))
        loadedVersion = Version.NONE
    }

    private constructor(history: List<GameEvent>) {
        check(history.isNotEmpty()) { "Provided history was empty. Create a new game instead." }

        id = history.first().gameId
        check(history.all { it.gameId == id }) { "GameId mismatch" }
        check(history.count { it is GameCreated } == 1) { "There was more than 1 game created event" }
        history.forEach { appendEvent(it).orThrow() }
        loadedVersion = Version.of(history.size - 1)
    }

    val id: GameId
    var state = GameState.new()
        private set

    private val _events = mutableListOf<GameEvent>()
    val events: List<GameEvent> get() = _events.toList()

    val loadedVersion: Version
    val newEvents get() = _events.drop(loadedVersion.value + 1)

    fun addPlayer(playerId: PlayerId): Result4k<Unit, AddPlayerErrorCode> =
        appendEvent(PlayerJoined(id, playerId))
            .filterOrThrow<Unit, GameErrorCode, AddPlayerErrorCode>()

    private fun appendEvent(event: GameEvent): Result4k<Unit, GameErrorCode> =
        state.apply(event).map { nextState ->
            state = nextState
            _events += event
        }

    companion object {
        const val MAXIMUM_PLAYER_COUNT = 6

        fun new() = Game()

        fun from(history: List<GameEvent>) = Game(history)
    }
}

sealed class GameErrorCode : RuntimeException()

sealed class AddPlayerErrorCode : GameErrorCode()

class GameIsFull : AddPlayerErrorCode()

class PlayerHasAlreadyJoined : AddPlayerErrorCode()
