package com.tamj0rd2.skullking.application.ports.output

import com.tamj0rd2.skullking.domain.Version
import com.tamj0rd2.skullking.domain.game.GameEvent
import com.tamj0rd2.skullking.domain.game.GameId

interface GameEventStore {
    fun append(newEvents: List<GameEvent>, expectedVersion: Version)

    fun read(gameId: GameId): List<GameEvent>

    fun subscribe(subscriber: GameEventSubscriber)
}

class CannotSaveEventsForMultipleGames : IllegalArgumentException("Cannot save events for multiple games at once")

class OptimisticLockingException :
    IllegalStateException("The game has been modified since you last loaded it. Please reload the game and try again.")

class GameNotFoundException(gameId: GameId) : NoSuchElementException("Game with ID $gameId not found")
