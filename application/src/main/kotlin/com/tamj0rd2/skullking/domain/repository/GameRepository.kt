package com.tamj0rd2.skullking.domain.repository

import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.port.output.GameEventsPort

class GameRepository(
    private val gameEventsPort: GameEventsPort,
) {
    fun load(gameId: GameId): Game {
        return Game(gameId, gameEventsPort.findGameEvents(gameId))
    }

    fun save(game: Game) {
        gameEventsPort.saveGameEvents(game.changes)
    }
}
