package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.game.GameAction.AddPlayer
import com.tamj0rd2.skullking.domain.game.GameAction.PlaceBid
import com.tamj0rd2.skullking.domain.game.GameAction.Start
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random

// TODO: this is a mess. Tidy it!

/**
 * Game is a DDD aggregate
 * It has a lifecycle - it starts when it is created, and ends when the game is completed.
 * It is a transactional boundary - all changes to any entities used by game must be ACID
 * It is a consistency boundary - all changes must either happen, or not.
 */
class Game {
    private constructor(createdBy: PlayerId) {
        id = GameId.random()
        appendEvents(GameCreatedEvent(gameId = id, createdBy = createdBy))
        loadedVersion = Version.NONE
    }

    private constructor(history: List<GameEvent>) {
        id = history.first().gameId
        check(history.all { it.gameId == id }) { "GameId mismatch" }
        check(history.count { it is GameCreatedEvent } == 1) { "There was more than 1 game created event" }
        appendEvents(*history.toTypedArray()).orThrow()
        loadedVersion = Version.of(history.size - 1)
    }

    val id: GameId
    var state = GameState.new()
        private set

    private val _events = mutableListOf<GameEvent>()
    val events: List<GameEvent> get() = _events.toList()

    val loadedVersion: Version
    val newEvents get() = _events.drop(loadedVersion.value + 1)

    fun execute(vararg actions: GameAction): Result4k<Unit, GameErrorCode> {
        require(actions.isNotEmpty()) { "Cannot execute empty actions." }
        actions.forEach { action ->
            when (action) {
                is AddPlayer -> addPlayer(action.playerId)
                is Start -> start()
                is PlaceBid -> placeBid(action)
            }.onFailure { return it }
        }
        return Unit.asSuccess()
    }

    private fun addPlayer(playerId: PlayerId): Result4k<Unit, GameErrorCode> = appendEvents(PlayerJoinedEvent(id, playerId))

    private fun start(): Result4k<Unit, GameErrorCode> {
        appendEvents(GameStartedEvent(id)).onFailure { return it }
        appendEvents(CardDealtEvent(id)).onFailure { return it }
        return Unit.asSuccess()
    }

    private fun placeBid(action: PlaceBid): Result4k<Unit, GameErrorCode> {
        appendEvents(BidPlacedEvent(id, action.playerId, action.bid)).onFailure { return it }
        return Unit.asSuccess()
    }

    private fun appendEvents(vararg events: GameEvent): Result4k<Unit, GameErrorCode> {
        state =
            events.fold(state) { state, event ->
                state.apply(event).onFailure { return it }
            }
        _events.addAll(events)
        return Unit.asSuccess()
    }

    companion object {
        const val MINIMUM_PLAYER_COUNT = 2
        const val MAXIMUM_PLAYER_COUNT = 6

        fun new(createdBy: PlayerId) = Game(createdBy)

        fun from(history: List<GameEvent>) = Game(history)
    }
}
