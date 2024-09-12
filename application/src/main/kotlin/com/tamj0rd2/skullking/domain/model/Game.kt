package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.skullking.port.output.GameEventsPort
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import java.util.UUID

@JvmInline
value class GameId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<GameId>(::GameId)
}

@Suppress("CONTEXT_RECEIVERS_DEPRECATED") // When contextParameters are available, I'll migrate.
class Game private constructor(
    val id: GameId,
) {
    private val _players = mutableListOf<PlayerId>()
    val players get() = _players.toList()

    fun addPlayer(playerId: PlayerId): Result4k<Unit, AddPlayerErrorCode> {
        if (players.size >= MAXIMUM_PLAYER_COUNT) return Failure(GameIsFull)
        _players.add(playerId)
        return Success(Unit)
    }

    companion object {
        const val MAXIMUM_PLAYER_COUNT = 6

        context(GameEventsPort)
        internal fun load(id: GameId): Game {
            val eventsForThisGame = findGameEvents(id)
            return eventsForThisGame.fold(Game(id)) { game, event ->
                when (event) {
                    is PlayerJoined -> game.apply { addPlayer(event.playerId) }
                }
            }
        }

        context(GameEventsPort)
        internal fun addPlayer(
            gameId: GameId,
            playerId: PlayerId,
        ): Result4k<Unit, AddPlayerErrorCode> {
            load(gameId).addPlayer(playerId).onFailure { return it }
            saveGameEvents(listOf(PlayerJoined(gameId, playerId)))
            return Success(Unit)
        }
    }
}

sealed interface GameErrorCode

sealed interface AddPlayerErrorCode : GameErrorCode

data object GameIsFull : AddPlayerErrorCode
