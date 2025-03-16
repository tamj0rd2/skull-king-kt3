package com.tamj0rd2.skullking.domain.game

sealed interface GameEvent {
    val gameId: GameId

    data class GameStarted(
        override val gameId: GameId,
        val players: Set<PlayerId>,
    ) : GameEvent

    data class RoundStarted(
        override val gameId: GameId,
        val roundNumber: RoundNumber,
        val dealtCards: CardsPerPlayer,
    ) : GameEvent

    data class BidPlaced(
        override val gameId: GameId,
        val bid: Bid,
        val placedBy: PlayerId,
    ) : GameEvent

    data class CardPlayed(
        override val gameId: GameId,
        val card: Card,
        val playedBy: PlayerId,
    ) : GameEvent

    data class TrickCompleted(
        override val gameId: GameId,
    ) : GameEvent

    data class RoundCompleted(
        override val gameId: GameId,
    ) : GameEvent

    data class GameCompleted(
        override val gameId: GameId,
    ) : GameEvent
}
