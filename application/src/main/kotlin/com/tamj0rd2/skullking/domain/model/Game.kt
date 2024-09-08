package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.skullking.port.output.GameEventsPort
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
    val players: List<PlayerId>,
) {
    companion object {
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
        ) {
            saveGameEvents(listOf(PlayerJoined(gameId, playerId)))
        }

        private fun new(id: GameId): Game =
            Game(
                id = id,
                players = emptyList(),
            )
    }
}
