package com.tamj0rd2.skullking.port.output

import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.domain.model.GameEvent
import com.tamj0rd2.skullking.domain.model.GameId

interface GameRepository {
    fun load(gameId: GameId): Game
    fun save(game: Game)

    fun findGameEvents(gameId: GameId): List<GameEvent>

    fun saveGameEvents(events: List<GameEvent>)
}
