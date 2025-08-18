package com.tamj0rd2.skullking.domain.game

data class Game
private constructor(val id: GameId, val events: List<GameEvent>, val loadedAtVersion: Int) {
    val newEvents = events.drop(loadedAtVersion)

    val creator
        get() = (events.first() as GameEvent.GameCreated).createdBy

    val players
        get() = events.filterIsInstance<GameEvent.PlayerJoined>().map { it.playerId }.toSet()

    companion object {
        fun new(id: GameId, createdBy: PlayerId): Game {
            return Game(
                id = id,
                events = listOf(GameEvent.GameCreated(gameId = id, createdBy = createdBy)),
                loadedAtVersion = 0,
            )
        }

        fun reconstitute(events: List<GameEvent>): Game {
            val gameId = events.first().gameId
            return Game(id = gameId, events = events, loadedAtVersion = events.size)
        }
    }

    fun addPlayer(playerId: PlayerId): Game {
        return copy(events = events + GameEvent.PlayerJoined(gameId = id, playerId = playerId))
    }
}

sealed interface GameEvent {
    val gameId: GameId

    data class GameCreated(override val gameId: GameId, val createdBy: PlayerId) : GameEvent

    data class PlayerJoined(override val gameId: GameId, val playerId: PlayerId) : GameEvent
}
