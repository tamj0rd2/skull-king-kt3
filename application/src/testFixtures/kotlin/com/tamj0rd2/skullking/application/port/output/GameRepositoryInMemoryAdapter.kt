package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.domain.model.GameEvent
import com.tamj0rd2.skullking.domain.model.GameId

class GameRepositoryInMemoryAdapter : GameRepository {
    private val savedEvents = mutableMapOf<GameId, List<GameEvent>>()

    override fun load(gameId: GameId): Game = Game.from(findGameEvents(gameId))

    override fun save(game: Game) {
        saveGameEvents(game.updates)
    }

    override fun findGameEvents(gameId: GameId): List<GameEvent> = savedEvents.getOrDefault(gameId, emptyList())

    override fun saveGameEvents(events: List<GameEvent>) {
        events.forEach {
            val updatedEvents = savedEvents.getOrDefault(it.gameId, emptyList()) + it
            savedEvents[it.gameId] = updatedEvents
        }
    }
}
