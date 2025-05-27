package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.gamev3.GamePhase.NotStarted
import com.tamj0rd2.skullking.domain.gamev3.GamePhase.SomeOtherPhase
import dev.forkhandles.result4k.Result4k

typealias GameStateResult = Result4k<GameState, GameErrorCode>

data class GameState private constructor(
    val players: Set<PlayerId>,
    val phase: GamePhase,
) {
    fun apply(event: GameEvent): GameStateResult =
        when (event) {
            is GameStartedEvent -> startGame(event)
            is RoundStartedEvent -> startRound(event)
        }

    private fun startGame(event: GameStartedEvent): GameStateResult = copy(players = event.players).asSuccess()

    private fun startRound(event: RoundStartedEvent): GameStateResult = copy(phase = SomeOtherPhase).asSuccess()

    companion object {
        val new =
            GameState(
                players = emptySet(),
                phase = NotStarted,
            )
    }
}

sealed interface GamePhase {
    data object NotStarted : GamePhase

    data object SomeOtherPhase : GamePhase
}
