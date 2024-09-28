package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.GameUpdate
import com.tamj0rd2.skullking.domain.model.PlayerId

interface GameUpdateNotifier {
    fun subscribe(
        gameId: GameId,
        playerId: PlayerId,
        listener: GameUpdateListener,
    )

    fun broadcast(updates: List<GameUpdate>)

    fun broadcast(vararg updates: GameUpdate) {
        require(updates.isNotEmpty()) { "list of updates was empty" }
        return broadcast(updates.toList())
    }
}
