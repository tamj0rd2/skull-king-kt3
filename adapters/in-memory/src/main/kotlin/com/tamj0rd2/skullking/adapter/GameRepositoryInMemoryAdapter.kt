package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.port.output.GameDoesNotExist
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.domain.model.game.Game
import com.tamj0rd2.skullking.domain.model.game.GameEvent
import com.tamj0rd2.skullking.domain.model.game.GameId

class GameRepositoryInMemoryAdapter : GameRepository {
    private val savedEvents = mutableMapOf<GameId, List<GameEvent>>()

    override fun load(gameId: GameId): Game {
        val events = savedEvents[gameId] ?: throw GameDoesNotExist()
        return Game.from(events)
    }

    override fun save(game: Game) {
        savedEvents[game.id] = game.events
    }
}
