package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.GameUpdate

interface GameUpdateNotifier {
    fun subscribe(
        gameId: GameId,
        listener: GameUpdateListener,
    )

    fun broadcast(
        gameId: GameId,
        updates: List<GameUpdate>,
    )

    fun broadcast(
        gameId: GameId,
        vararg updates: GameUpdate,
    ) {
        require(updates.isNotEmpty()) { "list of updates was empty" }
        return broadcast(gameId, updates.toList())
    }
}
