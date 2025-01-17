package com.tamj0rd2.skullking.domain.game

sealed class GameAction {
    data class AddPlayer(
        val playerId: PlayerId,
    ) : GameAction()

    data object Start : GameAction()

    data class PlaceBid(
        val playerId: PlayerId,
        val bid: Bid,
    ) : GameAction()
}
