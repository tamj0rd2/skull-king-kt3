package com.tamj0rd2.skullking.domain.model

sealed interface GameEvent {
    val gameId: GameId
}

data class PlayerJoined(
    override val gameId: GameId,
    val playerId: PlayerId,
) : GameEvent