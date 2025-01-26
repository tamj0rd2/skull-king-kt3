package com.tamj0rd2.skullking.domain.game

sealed class LobbyCommand {
    data class AddPlayer(
        val playerId: PlayerId,
    ) : LobbyCommand()

    data object Start : LobbyCommand()

    data class PlaceBid(
        val playerId: PlayerId,
        val bid: Bid,
    ) : LobbyCommand()
}
