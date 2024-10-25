package com.tamj0rd2.skullking.domain.game

sealed interface GameEvent {
    val gameId: GameId
}

data class GameCreatedEvent(
    override val gameId: GameId,
) : GameEvent

data class PlayerJoinedEvent(
    override val gameId: GameId,
    val playerId: PlayerId,
) : GameEvent

data class GameStartedEvent(
    override val gameId: GameId,
) : GameEvent

data class CardDealtEvent(
    override val gameId: GameId,
) : GameEvent
