package com.tamj0rd2.skullking.adapters.inmemory

import com.tamj0rd2.skullking.application.ports.output.GameEventSubscriber
import com.tamj0rd2.skullking.application.ports.output.GameRepository
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameEvent
import com.tamj0rd2.skullking.domain.game.GameId
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class InMemoryGameRepository() : GameRepository {
    private val events = mutableListOf<GameEvent>()
    private val games
        get() = events.groupBy { it.gameId }.mapValues { (_, events) -> Game.reconstitute(events) }

    private val eventSubscribers = mutableSetOf<GameEventSubscriber>()
    private val outbox = mutableListOf<GameEvent>()

    private val scheduler =
        Executors.newScheduledThreadPool(
            1,
            Thread.ofVirtual().name("inmem-game-scheduler-", 0).factory(),
        )

    init {
        // passing a lambda rather than a reference actually improves the stack trace
        scheduler.scheduleAtFixedRate({ processOutbox() }, 0, 1, TimeUnit.MILLISECONDS)
    }

    override fun save(game: Game) {
        events.addAll(game.newEvents)
        outbox.addAll(game.newEvents)
    }

    override fun load(gameId: GameId): Game? {
        return games[gameId]
    }

    override fun findAll(): List<Game> {
        return games.values.toList()
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
