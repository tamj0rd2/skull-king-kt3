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
    private constructor(history: List<GameEvent>) {
        gameActivityLog = GameActivityLog.forExistingGame(history)
        id = gameActivityLog.gameId

        history.forEach { event ->
            when (event) {
                is PlayerJoined -> addPlayer(event.playerId)
                is GameCreated -> Unit.asSuccess()
            }.orThrow()
        }

        gameActivityLog.startRecordingUpdates()
    }

    private constructor() {
        id = GameId.random()
        gameActivityLog = GameActivityLog.forNewGame()
        gameActivityLog.record(GameCreated(id))
    }

    val id: GameId

    private val gameActivityLog: GameActivityLog
    val history get() = gameActivityLog.history
    val updates get() = gameActivityLog.updates

    private val _players = mutableListOf<PlayerId>()
    val players: List<PlayerId> get() = _players

    fun addPlayer(playerId: PlayerId): Result4k<Unit, AddPlayerErrorCode> {
        if (players.size >= MAXIMUM_PLAYER_COUNT) return GameIsFull.asFailure()
        _players.add(playerId)
        gameActivityLog.record(PlayerJoined(id, playerId))
        return Unit.asSuccess()
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
