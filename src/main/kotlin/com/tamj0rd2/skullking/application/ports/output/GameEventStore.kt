package com.tamj0rd2.skullking.application.ports.output

import com.tamj0rd2.skullking.domain.game.GameEvent
import com.tamj0rd2.skullking.domain.game.GameId

interface GameEventStore {
    fun append(events: List<GameEvent>)

    fun read(gameId: GameId): List<GameEvent>

    fun subscribe(subscriber: GameEventSubscriber)
}
