package com.tamj0rd2.skullking.application.ports.output

import com.tamj0rd2.skullking.domain.game.GameEvent
import java.util.concurrent.CopyOnWriteArrayList

class SpyGameEventSubscriber : GameEventSubscriber {
    private val _events = CopyOnWriteArrayList<GameEvent>()

    val receivedEvents: List<GameEvent>
        get() = _events

    override fun notify(event: GameEvent) {
        _events.add(event)
    }
}
