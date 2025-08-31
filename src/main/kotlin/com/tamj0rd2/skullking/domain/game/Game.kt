package com.tamj0rd2.skullking.domain.game

data class Game
private constructor(
    val id: GameId,
    val creator: PlayerId,
    val events: List<GameEvent> = emptyList(),
    val players: Set<PlayerId> = emptySet(),
    val roundNumber: RoundNumber? = null,
) {
    companion object {
        fun new(id: GameId, createdBy: PlayerId): Game {
            return Game(id = id, creator = createdBy).applyEvent(GameEvent.GameCreated(gameId = id, createdBy = createdBy))
        }

        fun reconstitute(events: List<GameEvent>): Game {
            val creationEvent = events.first() as GameEvent.GameCreated
            return events.fold(Game(id = creationEvent.gameId, creator = creationEvent.createdBy), Game::applyEvent)
        }
    }

    fun addPlayer(playerId: PlayerId): Game {
        return applyEvent(GameEvent.PlayerJoined(gameId = id, playerId = playerId))
    }

    fun start(): Game {
        return copy(events = events + GameEvent.GameStarted(gameId = id), roundNumber = RoundNumber.One)
    }

    private fun applyEvent(event: GameEvent): Game {
        return when (event) {
            is GameEvent.GameCreated -> copy(creator = event.createdBy, players = setOf(event.createdBy))

            is GameEvent.PlayerJoined -> copy(players = players + event.playerId)

            is GameEvent.GameStarted -> this
        }.copy(events = events + event)
    }
}

sealed interface GameEvent {
    val gameId: GameId

    data class GameCreated(override val gameId: GameId, val createdBy: PlayerId) : GameEvent

    data class PlayerJoined(override val gameId: GameId, val playerId: PlayerId) : GameEvent

    data class GameStarted(override val gameId: GameId) : GameEvent
}

enum class RoundNumber {
    One,
    Two,
    Three,
    Four,
    Five,
    Six,
    Seven,
    Eight,
    Nine,
    Ten,
}
