package com.tamj0rd2.skullking.domain.game

sealed interface LobbyNotification {
    data class APlayerHasJoined(
        val playerId: PlayerId,
    ) : LobbyNotification

    data object TheGameHasStarted : LobbyNotification

    data class ACardWasDealt(
        val card: Card,
    ) : LobbyNotification

    data class ABidWasPlaced(
        val playerId: PlayerId,
    ) : LobbyNotification

    data class AllBidsHaveBeenPlaced(
        val bids: Map<PlayerId, Bid>,
    ) : LobbyNotification

    data class ACardWasPlayed(
        val playedCard: PlayedCard,
    ) : LobbyNotification

    data class TheTrickHasEnded(
        val winner: PlayerId,
    ) : LobbyNotification
}
