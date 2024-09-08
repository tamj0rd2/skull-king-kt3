package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.skullking.port.output.GameEventsPort
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
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
@ConsistentCopyVisibility
data class Game private constructor(
    val id: GameId,
    val players: List<PlayerId> = emptyList(),
) {
    companion object {
        const val MAXIMUM_PLAYER_COUNT = 6

        context(GameEventsPort)
        internal fun load(id: GameId): Game {
            val eventsForThisGame = findGameEvents(id)
            return eventsForThisGame.fold(new(id)) { game, event ->
                when (event) {
                    is PlayerJoined -> game.copy(players = game.players + event.playerId)
                }
            }
        }

        context(GameEventsPort)
        internal fun addPlayer(
            gameId: GameId,
            playerId: PlayerId,
        ): Result4k<Unit, AddPlayerErrorCode> {
            val game = load(gameId)
            if (game.players.size >= MAXIMUM_PLAYER_COUNT) return Failure(GameIsFull)
            saveGameEvents(listOf(PlayerJoined(gameId, playerId)))
            return Success(Unit)
        }

        private fun new(id: GameId): Game =
            Game(
                id = id,
                players = emptyList(),
            )
    }
}

sealed interface GameErrorCode

sealed interface AddPlayerErrorCode : GameErrorCode

data object GameIsFull : AddPlayerErrorCode
