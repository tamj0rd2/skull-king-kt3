package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asSuccess
import dev.forkhandles.result4k.Result4k

data class GameState private constructor(
    val players: List<PlayerId>,
    val bids: Map<PlayerId, Bid>,
) {
    val allBidsHaveBeenPlaced get() = bids.keys == players.toSet()

    fun apply(event: BidPlacedEvent): Result4k<GameState, LobbyErrorCode> = copy(bids = bids + Pair(event.playerId, event.bid)).asSuccess()

    companion object {
        fun new(players: List<PlayerId>) =
            GameState(
                players = players,
                bids = emptyMap(),
            )
    }
}
