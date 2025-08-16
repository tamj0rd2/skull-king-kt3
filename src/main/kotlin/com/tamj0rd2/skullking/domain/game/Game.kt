package com.tamj0rd2.skullking.domain.game

data class Game
private constructor(
    val id: GameId,
    val creator: PlayerId,
    val players: Set<PlayerId>,
    val events: List<GameEvent>,
) {
    companion object {
        fun new(id: GameId, createdBy: PlayerId): Game {
            return Game(id = id, creator = createdBy, players = emptySet(), events = emptyList())
        }
    }

    fun addPlayer(playerId: PlayerId): Game {
        return copy(
            players = players + playerId,
            events = events + GameEvent.PlayerJoined(gameId = id, playerId = playerId),
        )
    }
}

sealed interface GameEvent {
    val gameId: GameId

    data class PlayerJoined(override val gameId: GameId, val playerId: PlayerId) : GameEvent
}
