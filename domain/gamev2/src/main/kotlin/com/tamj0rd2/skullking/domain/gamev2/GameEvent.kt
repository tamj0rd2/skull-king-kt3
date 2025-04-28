package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.skullking.domain.gamev2.values.Bid
import com.tamj0rd2.skullking.domain.gamev2.values.RoundNumber
import com.tamj0rd2.skullking.domain.gamev2.values.TrickNumber

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

    data class TrickStarted(
        override val gameId: GameId,
        val trickNumber: TrickNumber,
    ) : GameEvent

    data class CardPlayed(
        override val gameId: GameId,
        val card: Card,
        val playedBy: PlayerId,
    ) : GameEvent

    data class TrickCompleted(
        override val gameId: GameId,
        val trickNumber: TrickNumber,
    ) : GameEvent

    data class RoundCompleted(
        override val gameId: GameId,
        val roundNumber: RoundNumber,
    ) : GameEvent

    data class GameCompleted(
        override val gameId: GameId,
    ) : GameEvent
}
