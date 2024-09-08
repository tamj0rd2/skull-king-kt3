package com.tamj0rd2.skullking.port.output

import com.tamj0rd2.skullking.domain.model.GameEvent
import com.tamj0rd2.skullking.domain.model.GameId

class GameEventsInMemoryAdapter : GameEventsPort {
    private val savedEvents = mutableMapOf<GameId, List<GameEvent>>()

    override fun findGameEvents(gameId: GameId): List<GameEvent> = savedEvents.getOrDefault(gameId, emptyList())

    override fun saveGameEvents(events: List<GameEvent>) {
        events.forEach {
            val updatedEvents = savedEvents.getOrDefault(it.gameId, emptyList()) + it
            savedEvents[it.gameId] = updatedEvents
        }
    }
}
