package com.tamj0rd2.skullking.application.repositories

import com.tamj0rd2.skullking.application.ports.output.GameEventStore
import com.tamj0rd2.skullking.application.ports.output.GameEventSubscriber
import com.tamj0rd2.skullking.domain.Version
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameId

class GameRepository(private val eventStore: GameEventStore) {
    private val gameIds = mutableSetOf<GameId>()

    init {
        subscribe { event -> gameIds.add(event.gameId) }
    }

    fun save(game: Game, expectedVersion: Version) {
        val newEvents = game.events.drop(expectedVersion.value)
        eventStore.append(newEvents, expectedVersion)
    }

    fun load(gameId: GameId): Pair<Game, Version> {
        val events = eventStore.read(gameId)
        val game = Game.reconstitute(events)
        return game to game.version
    }

    fun findAll(): List<Game> {
        return gameIds.map { load(it).first }
    }

    fun subscribe(subscriber: GameEventSubscriber) {
        eventStore.subscribe(subscriber)
    }
}
