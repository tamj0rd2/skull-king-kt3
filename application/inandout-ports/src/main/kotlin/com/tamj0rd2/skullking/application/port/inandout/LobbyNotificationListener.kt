package com.tamj0rd2.skullking.application.port.inandout

import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.Card
import com.tamj0rd2.skullking.domain.game.PlayedCard
import com.tamj0rd2.skullking.domain.game.PlayerId

fun interface LobbyNotificationListener {
    fun receive(updates: List<LobbyNotification>)

    fun receive(vararg updates: LobbyNotification) {
        require(updates.isNotEmpty()) { "must send at least 1 game update" }
        receive(updates.toList())
    }
}

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
