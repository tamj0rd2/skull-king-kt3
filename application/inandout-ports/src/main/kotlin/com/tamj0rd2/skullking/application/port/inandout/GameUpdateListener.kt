package com.tamj0rd2.skullking.application.port.inandout

import com.tamj0rd2.skullking.domain.game.Card
import com.tamj0rd2.skullking.domain.game.PlayerId

fun interface GameUpdateListener {
    fun receive(updates: List<GameUpdate>)

    fun receive(vararg updates: GameUpdate) {
        require(updates.isNotEmpty()) { "must send at least 1 game update" }
        receive(updates.toList())
    }
}

sealed interface GameUpdate {
    data class PlayerJoined(
        val playerId: PlayerId,
    ) : GameUpdate

    data object GameStarted : GameUpdate

    data class CardDealt(
        val card: Card,
    ) : GameUpdate

    data class BidMade(
        val playerId: PlayerId,
    ) : GameUpdate
}
