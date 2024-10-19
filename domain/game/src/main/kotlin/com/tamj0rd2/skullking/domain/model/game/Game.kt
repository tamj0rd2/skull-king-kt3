package com.tamj0rd2.skullking.domain.model.game

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.extensions.filterOrThrow
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.StartGameErrorCode.TooFewPlayers
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure
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
    companion object : UUIDValueFactory<GameId>(::GameId, validation = { it != UUID(0, 0) }) {
        val NONE = GameId(UUID(0, 0))
    }
}

@JvmInline
value class Version private constructor(
    override val value: Int,
) : Value<Int> {
    companion object : IntValueFactory<Version>(::Version, 0.minValue) {
        val NONE = Version(-1)
    }
}

/**
 * Game is a DDD aggregate
 * It has a lifecycle - it starts when it is created, and ends when the game is completed.
 * It is a transactional boundary - all changes to any entities used by game must be ACID
 * It is a consistency boundary - all changes must either happen, or not.
 */
class Game {
    private constructor() {
        id = GameId.random()
        appendEvent(GameCreatedEvent(id))
        loadedVersion = Version.NONE
    }

    private constructor(history: List<GameEvent>) {
        id = history.first().gameId
        check(history.all { it.gameId == id }) { "GameId mismatch" }
        check(history.count { it is GameCreatedEvent } == 1) { "There was more than 1 game created event" }
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
        appendEvent(PlayerJoinedEvent(id, playerId)).filterOrThrow<AddPlayerErrorCode>()

    fun start(): Result4k<Unit, GameErrorCode> {
        appendEvent(GameStartedEvent(id)).filterOrThrow<StartGameErrorCode>().onFailure { return it }
        appendEvent(CardDealtEvent(id)).filterOrThrow<DealCardErrorCode>().onFailure { return it }
        return Unit.asSuccess()
    }

    private fun appendEvent(event: GameEvent): Result4k<Unit, GameErrorCode> {
        val nextState =
            when (event) {
                is GameCreatedEvent -> state.asSuccess()

                is PlayerJoinedEvent ->
                    state.run {
                        if (players.size >= MAXIMUM_PLAYER_COUNT) return GameIsFull().asFailure()
                        if (players.contains(event.playerId)) return PlayerHasAlreadyJoined().asFailure()
                        copy(players = players + event.playerId).asSuccess()
                    }

                is GameStartedEvent ->
                    state.run {
                        if (players.size < MINIMUM_PLAYER_COUNT) return TooFewPlayers().asFailure()
                        this.asSuccess()
                    }

                is CardDealtEvent ->
                    state.run {
                        this.asSuccess()
                    }
            }.onFailure { return it }

        state = nextState
        _events += event

        return Unit.asSuccess()
    }

    companion object {
        const val MINIMUM_PLAYER_COUNT = 2
        const val MAXIMUM_PLAYER_COUNT = 6

        fun new() = Game()

        fun from(history: List<GameEvent>) = Game(history)
    }
}

private inline fun <reified E : GameErrorCode> Result4k<Unit, GameErrorCode>.filterOrThrow(): Result4k<Unit, E> =
    filterOrThrow<Unit, GameErrorCode, E>()

sealed class GameErrorCode : RuntimeException()

sealed class AddPlayerErrorCode : GameErrorCode()

class GameIsFull : AddPlayerErrorCode()

class PlayerHasAlreadyJoined : AddPlayerErrorCode()

sealed class StartGameErrorCode : GameErrorCode() {
    class TooFewPlayers : StartGameErrorCode()
}

class DealCardErrorCode : GameErrorCode()
