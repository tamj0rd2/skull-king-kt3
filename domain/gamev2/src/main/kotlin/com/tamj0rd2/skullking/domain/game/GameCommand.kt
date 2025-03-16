package com.tamj0rd2.skullking.domain.game

sealed interface GameCommand {
    data class StartRound(
        val roundNumber: RoundNumber,
    ) : GameCommand

    data class PlaceABid(
        val bid: Bid,
        val actor: PlayerId,
    ) : GameCommand
}
