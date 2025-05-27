package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.extensions.asSuccess
import dev.forkhandles.result4k.Result4k

typealias GameStateResult = Result4k<GameState, GameErrorCode>

data class GameState private constructor(
    val players: Set<PlayerId>,
) {
    fun apply(event: GameEvent): GameStateResult =
        when (event) {
            is GameStartedEvent -> startGame(event)
        }

    private fun startGame(event: GameStartedEvent): GameStateResult = copy(players = event.players).asSuccess()

    companion object {
        val new = GameState(players = emptySet())
    }
}
