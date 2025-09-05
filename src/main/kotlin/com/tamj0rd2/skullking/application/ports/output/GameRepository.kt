package com.tamj0rd2.skullking.application.ports.output

import com.tamj0rd2.skullking.domain.Version
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameEvent
import com.tamj0rd2.skullking.domain.game.GameId

interface GameRepository : SaveGamePort, LoadGamePort, FindGamesPort, SubscribeToGameEventsPort

fun interface SaveGamePort {
    fun save(game: Game, expectedVersion: Version)
}

fun interface LoadGamePort {
    fun load(gameId: GameId): Pair<Game, Version>
}

fun interface FindGamesPort {
    fun findAll(): List<Game>
}

fun interface SubscribeToGameEventsPort {
    fun subscribe(subscriber: GameEventSubscriber)
}

fun interface GameEventSubscriber {
    fun notify(event: GameEvent)
}
