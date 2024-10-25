package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.output.GameDoesNotExist
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameEvent
import com.tamj0rd2.skullking.domain.game.GameId

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
