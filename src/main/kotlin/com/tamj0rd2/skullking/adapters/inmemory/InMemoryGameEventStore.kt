package com.tamj0rd2.skullking.adapters.inmemory

import com.tamj0rd2.skullking.application.ports.output.GameEventStore
import com.tamj0rd2.skullking.application.ports.output.GameEventSubscriber
import com.tamj0rd2.skullking.domain.game.GameEvent
import com.tamj0rd2.skullking.domain.game.GameId
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class InMemoryGameEventStore : GameEventStore {
    private val events = mutableListOf<GameEvent>()
    private val eventSubscribers = mutableSetOf<GameEventSubscriber>()
    private val outbox = mutableListOf<GameEvent>()
    private val scheduler = Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory())

    init {
        // passing a lambda rather than a reference actually improves the stack trace
        scheduler.scheduleAtFixedRate({ processOutbox() }, 0, 1, TimeUnit.MILLISECONDS)
    }

    override fun append(events: List<GameEvent>) {
        this.events.addAll(events)
        outbox.addAll(events)
    }

    override fun read(gameId: GameId): List<GameEvent> {
        return events.filter { it.gameId == gameId }
    }

    override fun subscribe(subscriber: GameEventSubscriber) {
        eventSubscribers.add(subscriber)
    }

    private fun processOutbox() {
        val event = outbox.removeFirstOrNull() ?: return

        try {
            eventSubscribers.forEach { it.notify(event) }
        } catch (t: Throwable) {
            t.printStackTrace()
            outbox.addFirst(event)
        }
    }
}
