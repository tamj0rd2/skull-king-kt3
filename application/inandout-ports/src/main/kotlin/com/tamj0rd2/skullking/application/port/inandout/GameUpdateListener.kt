package com.tamj0rd2.skullking.application.port.inandout

import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.Card
import com.tamj0rd2.skullking.domain.game.PlayedCard
import com.tamj0rd2.skullking.domain.game.PlayerId

fun interface GameUpdateListener {
    fun receive(updates: List<GameUpdate>)

    fun receive(vararg updates: GameUpdate) {
        require(updates.isNotEmpty()) { "must send at least 1 game update" }
        receive(updates.toList())
    }
}

sealed interface GameUpdate {
    data class APlayerHasJoined(
        val playerId: PlayerId,
    ) : GameUpdate

    data object TheGameHasStarted : GameUpdate

    data class ACardWasDealt(
        val card: Card,
    ) : GameUpdate

    data class ABidWasPlaced(
        val playerId: PlayerId,
    ) : GameUpdate

    data class AllBidsHaveBeenPlaced(
        val bids: Map<PlayerId, Bid>,
    ) : GameUpdate

    data class ACardWasPlayed(
        val playedCard: PlayedCard,
    ) : GameUpdate

    data class TheTrickHasEnded(
        val winner: PlayerId,
    ) : GameUpdate
}
