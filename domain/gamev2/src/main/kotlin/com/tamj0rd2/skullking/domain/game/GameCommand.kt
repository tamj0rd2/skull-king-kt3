package com.tamj0rd2.skullking.domain.game

sealed interface GameCommand {
    data class StartRound(
        val roundNumber: RoundNumber,
    ) : GameCommand

    data class PlaceABid(
        val bid: Bid,
        val actor: PlayerId,
    ) : GameCommand

    data class StartTrick(
        val trickNumber: TrickNumber,
    ) : GameCommand

    data class PlayACard(
        val card: Card,
        val actor: PlayerId,
    ) : GameCommand

    data class CompleteTrick(
        val trickNumber: TrickNumber,
    ) : GameCommand

    data class CompleteRound(
        val roundNumber: RoundNumber,
    ) : GameCommand

    data object CompleteGame : GameCommand
}
