package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.values.random

data class Game private constructor(
    val id: GameId,
    val events: List<GameEvent>,
    val state: GameState,
) {
    fun execute(command: GameCommand): GameResult = copy().asSuccess()

    private fun appendEvent(event: GameEvent): GameResult = copy(events = events + event).asSuccess()

    companion object {
        fun new(players: Set<PlayerId>): GameResult {
            when {
                players.size < 2 -> return GameErrorCode.NotEnoughPlayers.asFailure()
                players.size > 6 -> return GameErrorCode.TooManyPlayers.asFailure()
            }

            return Game(
                id = SomeGameId.random(),
                events = emptyList(),
                state = GameState(players = players),
            ).appendEvent(GameStartedEvent)
        }
    }
}

data class GameState(
    val players: Set<PlayerId>,
)

typealias GameResult = Result4k<Game, GameErrorCode>

enum class GameErrorCode {
    NotEnoughPlayers,
    TooManyPlayers,
}
