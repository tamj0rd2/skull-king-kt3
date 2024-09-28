package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.GameUpdate

interface GameRepository {
    fun load(gameId: GameId): Game

    fun save(game: Game)
}

interface GameUpdateListener {
    fun notify(updates: List<GameUpdate>)
}
