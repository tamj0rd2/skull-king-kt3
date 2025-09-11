package com.tamj0rd2.skullking.domain.game

sealed interface GameEvent {
    val gameId: GameId

    data class GameCreated(override val gameId: GameId, val createdBy: PlayerId) : GameEvent

    data class PlayerJoined(override val gameId: GameId, val playerId: PlayerId) : GameEvent

    data class RoundStarted(override val gameId: GameId, val roundNumber: RoundNumber) : GameEvent

    data class BidPlaced(override val gameId: GameId, val playerId: PlayerId, val bid: Bid) : GameEvent
}
