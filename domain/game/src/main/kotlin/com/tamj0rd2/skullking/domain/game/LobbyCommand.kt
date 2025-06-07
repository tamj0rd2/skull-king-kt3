package com.tamj0rd2.skullking.domain.game

sealed class LobbyCommand {
    data class AddPlayer(val playerId: PlayerId) : LobbyCommand()

    data object StartGame : LobbyCommand()

    data class PlaceBid(val playerId: PlayerId, val bid: Bid) : LobbyCommand()

    data class PlayACard(val playerId: PlayerId, val card: Card) : LobbyCommand()
}
