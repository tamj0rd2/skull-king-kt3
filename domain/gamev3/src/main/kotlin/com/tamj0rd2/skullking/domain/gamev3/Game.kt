package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.values.random

data class Game private constructor(
    val id: GameId,
    val state: GameState,
) {
    companion object {
        fun new(players: Set<PlayerId>): Result4k<Game, GameErrorCode> {
            when {
                players.size < 2 -> return GameErrorCode.NotEnoughPlayers.asFailure()
                players.size > 6 -> return GameErrorCode.TooManyPlayers.asFailure()
            }

            return Game(
                id = SomeGameId.random(),
                state = GameState(players = players),
            ).asSuccess()
        }
    }
}

data class GameState(
    val players: Set<PlayerId>,
)

enum class GameErrorCode {
    NotEnoughPlayers,
    TooManyPlayers,
}
