package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import dev.forkhandles.result4k.Result4k

data class GameState private constructor(
    val players: List<PlayerId>,
) {
    internal fun apply(event: PlayerJoined): Result4k<GameState, AddPlayerErrorCode> {
        if (players.size >= Game.MAXIMUM_PLAYER_COUNT) return GameIsFull.asFailure()
        if (players.contains(event.playerId)) return PlayerHasAlreadyJoined.asFailure()
        return copy(players = players + event.playerId).asSuccess()
    }

    internal fun apply(event: GameEvent): Result4k<GameState, AddPlayerErrorCode> =
        when (event) {
            is GameCreated -> this.asSuccess()
            is PlayerJoined -> apply(event)
        }

    companion object {
        internal fun new() =
            GameState(
                players = emptyList(),
            )
    }
}
