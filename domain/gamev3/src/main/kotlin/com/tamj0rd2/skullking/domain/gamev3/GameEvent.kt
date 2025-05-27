package com.tamj0rd2.skullking.domain.gamev3

sealed interface GameEvent {
    val id: GameId
}

data class GameStartedEvent(
    override val id: GameId,
    val players: Set<PlayerId>,
) : GameEvent

data class RoundStartedEvent(
    override val id: GameId,
) : GameEvent
