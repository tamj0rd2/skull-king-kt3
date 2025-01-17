package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.game.GameAction.AddPlayer
import com.tamj0rd2.skullking.domain.game.GameAction.PlaceBid
import com.tamj0rd2.skullking.domain.game.GameAction.Start
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random

/**
 * Game is a DDD aggregate
 * It has a lifecycle - it starts when it is created, and ends when the game is completed.
 * It is a transactional boundary - all changes to any entities used by game must be ACID
 * It is a consistency boundary - all changes must either happen, or not.
 */
class Game private constructor(
    val id: GameId,
    val loadedAtVersion: Version,
) {
    var state = GameState.new()
        private set

    private val _allEvents = mutableListOf<GameEvent>()
    val allEvents: List<GameEvent> get() = _allEvents.toList()
    val newEventsSinceGameWasLoaded get() = _allEvents.drop(loadedAtVersion.value)

    private constructor(createdBy: PlayerId) : this(
        id = GameId.random(),
        loadedAtVersion = Version.NONE,
    ) {
        appendEvents(GameCreatedEvent(gameId = id, createdBy = createdBy))
    }

    private constructor(history: List<GameEvent>) : this(
        id = history.first().gameId,
        loadedAtVersion = Version.of(history.size),
    ) {
        check(history.all { it.gameId == id }) { "GameId mismatch" }
        check(history.count { it is GameCreatedEvent } == 1) { "There was more than 1 game created event" }
        appendEvents(*history.toTypedArray()).orThrow()
    }

    private fun appendEvents(vararg events: GameEvent): Result4k<Unit, GameErrorCode> {
        state =
            events.fold(state) { state, event ->
                state.apply(event).onFailure { return it }
            }
        _allEvents.addAll(events)
        return Unit.asSuccess()
    }

    fun execute(action: GameAction): Result4k<Unit, GameErrorCode> =
        when (action) {
            is AddPlayer ->
                appendEvents(
                    PlayerJoinedEvent(id, action.playerId),
                )

            is Start ->
                appendEvents(
                    GameStartedEvent(id),
                    CardDealtEvent(id),
                )

            is PlaceBid ->
                appendEvents(
                    BidPlacedEvent(id, action.playerId, action.bid),
                )
        }

    companion object {
        const val MINIMUM_PLAYER_COUNT = 2
        const val MAXIMUM_PLAYER_COUNT = 6

        fun new(createdBy: PlayerId) = Game(createdBy)

        fun from(history: List<GameEvent>) = Game(history)
    }
}
