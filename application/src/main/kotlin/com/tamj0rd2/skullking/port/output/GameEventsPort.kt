package com.tamj0rd2.skullking.port.output

import com.tamj0rd2.skullking.domain.model.GameEvent
import com.tamj0rd2.skullking.domain.model.GameId

interface GameEventsPort {
    fun find(gameId: GameId): List<GameEvent>
    fun save(events: List<GameEvent>)
}
