package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.game.LobbyCommand.AddPlayer
import com.tamj0rd2.skullking.domain.game.LobbyCommand.PlaceBid
import com.tamj0rd2.skullking.domain.game.LobbyCommand.PlayACard
import com.tamj0rd2.skullking.domain.game.LobbyCommand.StartGame
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random

/**
 * Lobby is a DDD aggregate
 * It has a lifecycle - it starts when it is created, and ends when the game is completed.
 * It is a transactional boundary - all changes to any entities used by game must be ACID
 * It is a consistency boundary - all changes must either happen, or not.
 */
class Lobby private constructor(
    val id: LobbyId,
    val loadedAtVersion: Version,
) {
    var state = LobbyState.new()
        private set

    private val _allEvents = mutableListOf<LobbyEvent>()
    val allEvents: List<LobbyEvent> get() = _allEvents.toList()
    val newEventsSinceLobbyWasLoaded get() = _allEvents.drop(loadedAtVersion.value)

    private constructor(createdBy: PlayerId) : this(
        id = LobbyId.random(),
        loadedAtVersion = Version.NONE,
    ) {
        appendEvents(LobbyCreatedEvent(aggregateId = id, createdBy = createdBy))
    }

    private constructor(history: List<LobbyEvent>) : this(
        id = history.first().aggregateId,
        loadedAtVersion = Version.of(history.size),
    ) {
        check(history.all { it.aggregateId == id }) { "LobbyId mismatch" }
        check(history.count { it is LobbyCreatedEvent } == 1) { "There was more than 1 game created event" }
        appendEvents(*history.toTypedArray()).orThrow()
    }

    private fun appendEvents(vararg events: LobbyEvent): Result4k<Unit, LobbyErrorCode> {
        state =
            events.fold(state) { state, event ->
                state.apply(event).onFailure { return it }
            }
        _allEvents.addAll(events)
        return Unit.asSuccess()
    }

    fun execute(command: LobbyCommand): Result4k<Unit, LobbyErrorCode> =
        when (command) {
            is AddPlayer ->
                appendEvents(
                    PlayerJoinedEvent(id, command.playerId),
                )

            is StartGame -> {
                appendEvents(
                    GameStartedEvent(id),
                    RoundStartedEvent(id, state.players.associateWith { listOf(Card) }),
                )
            }

            is PlaceBid ->
                appendEvents(
                    BidPlacedEvent(id, command.playerId, command.bid),
                )

            is PlayACard ->
                appendEvents(
                    CardPlayedEvent(id, command.playerId, command.card),
                )
        }

    companion object {
        const val MINIMUM_PLAYER_COUNT = 2
        const val MAXIMUM_PLAYER_COUNT = 6

        fun new(createdBy: PlayerId) = Lobby(createdBy)

        fun from(history: List<LobbyEvent>) = Lobby(history)
    }
}
