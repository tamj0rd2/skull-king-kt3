package com.tamj0rd2.skullking.port.output

import com.tamj0rd2.skullking.domain.model.GameEvent
import com.tamj0rd2.skullking.domain.model.GameId

interface GameEventsPort {
    fun findGameEvents(gameId: GameId): List<GameEvent>

    fun saveGameEvents(events: List<GameEvent>)

    fun saveGameEvents(vararg event: GameEvent) = saveGameEvents(event.toList())
}
