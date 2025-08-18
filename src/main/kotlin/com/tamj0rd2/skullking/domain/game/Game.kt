package com.tamj0rd2.skullking.domain.game

data class Game
private constructor(val id: GameId, val players: Set<PlayerId>, val events: List<GameEvent>) {
    val creator
        get() = (events.first() as GameEvent.GameCreated).createdBy

    companion object {
        fun new(id: GameId, createdBy: PlayerId): Game {
            return Game(
                id = id,
                players = emptySet(),
                events = listOf(GameEvent.GameCreated(gameId = id, createdBy = createdBy)),
            )
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

    data class GameCreated(override val gameId: GameId, val createdBy: PlayerId) : GameEvent

    data class PlayerJoined(override val gameId: GameId, val playerId: PlayerId) : GameEvent
}
