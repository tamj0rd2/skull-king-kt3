package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.values.random

typealias GameResult = Result4k<Game, GameErrorCode>

data class Game
private constructor(
    val id: GameId,
    val events: List<GameEvent> = emptyList(),
    val state: GameState = GameState.NotStarted,
) {
    companion object {
        fun new(players: Set<PlayerId>): GameResult {
            val game = Game(SomeGameId.random())
            return GameStartedEvent.new(game.id, players).flatMap(game::appendEvent)
        }

        fun reconstitute(events: List<GameEvent>): GameResult {
            when {
                events.isEmpty() -> return GameErrorCode.CannotReconstituteGame.NoEvents.asFailure()
                events.first() !is GameStartedEvent ->
                    return GameErrorCode.CannotReconstituteGame.InvalidFirstEvent.asFailure()
                events.map { it.id }.toSet().size > 1 ->
                    return GameErrorCode.CannotReconstituteGame.MultipleGameIds.asFailure()
            }

            val initial = Game((events.first() as GameStartedEvent).id)
            return events.fold(initial.asSuccess() as GameResult) { result, event ->
                result.flatMap { it.appendEvent(event) }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Game) return false
        return other.id == id
    }

    override fun hashCode(): Int = id.hashCode()

    fun execute(command: GameCommand): GameResult =
        when (command) {
            is StartRoundCommand -> appendEvent(RoundStartedEvent(id))
            is PlaceBidCommand -> appendEvent(BidPlacedEvent(id, command.playerId, command.bid))
            is StartTrickCommand -> appendEvent(TrickStartedEvent(id))
            is PlayCardCommand -> appendEvent(CardPlayedEvent(id, command.playerId, command.card))
        }

    private fun appendEvent(event: GameEvent): GameResult {
        return copy(
                events = events + event,
                state =
                    state.apply(event).onFailure {
                        return it
                    },
            )
            .asSuccess()
    }
}
