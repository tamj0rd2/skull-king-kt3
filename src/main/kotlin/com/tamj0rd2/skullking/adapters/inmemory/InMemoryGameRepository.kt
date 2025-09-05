package com.tamj0rd2.skullking.adapters.inmemory

import com.tamj0rd2.skullking.application.ports.output.GameEventSubscriber
import com.tamj0rd2.skullking.application.ports.output.GameRepository
import com.tamj0rd2.skullking.domain.Version
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameId

class InMemoryGameRepository : GameRepository {
    private val eventStore = InMemoryGameEventStore()
    private val gameIds = mutableSetOf<GameId>()

    init {
        subscribe { event -> gameIds.add(event.gameId) }
    }

    override fun save(game: Game, expectedVersion: Version) {
        val newEvents = game.events.drop(expectedVersion.value)
        eventStore.append(newEvents, expectedVersion)
    }

    override fun load(gameId: GameId): Pair<Game, Version> {
        val events = eventStore.read(gameId)
        val game = Game.reconstitute(events)
        return game to game.version
    }

    override fun findAll(): List<Game> {
        return gameIds.map { load(it).first }
    }

    override fun subscribe(subscriber: GameEventSubscriber) {
        eventStore.subscribe(subscriber)
    }
}
