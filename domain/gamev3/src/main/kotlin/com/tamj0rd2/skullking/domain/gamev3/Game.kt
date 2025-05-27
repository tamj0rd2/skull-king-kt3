package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.values.random

typealias GameResult = Result4k<Game, GameErrorCode>

data class Game private constructor(
    val id: GameId,
    val events: List<GameEvent> = emptyList(),
    val state: GameState = GameState.new,
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Game) return false
        return other.id == id
    }

    override fun hashCode(): Int = id.hashCode()

    fun execute(command: GameCommand): GameResult =
        when (command) {
            StartRoundCommand -> appendEvent(RoundStartedEvent(id))
        }

    private fun appendEvent(event: GameEvent): GameResult {
        return copy(
            events = events + event,
            state = state.apply(event).onFailure { return it },
        ).asSuccess()
    }

    companion object {
        fun new(players: Set<PlayerId>): GameResult {
            when {
                players.size < 2 -> return GameErrorCode.NotEnoughPlayers.asFailure()
                players.size > 6 -> return GameErrorCode.TooManyPlayers.asFailure()
            }

            val id = SomeGameId.random()
            return Game(id).appendEvent(GameStartedEvent(id, players))
        }

        fun reconstitute(events: List<GameEvent>): GameResult {
            val initial = Game((events.first() as GameStartedEvent).id)
            return events.fold(initial.asSuccess() as GameResult) { result, event ->
                result.flatMap { it.appendEvent(event) }
            }
        }
    }
}
