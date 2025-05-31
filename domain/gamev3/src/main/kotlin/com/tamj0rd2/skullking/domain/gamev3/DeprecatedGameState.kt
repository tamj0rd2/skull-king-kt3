package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.gamev3.GamePhase.AwaitingNextRound
import com.tamj0rd2.skullking.domain.gamev3.GamePhase.Bidding
import com.tamj0rd2.skullking.domain.gamev3.GamePhase.NotStarted
import dev.forkhandles.result4k.Result4k

typealias GameStateResult = Result4k<DeprecatedGameState, GameErrorCode>

data class DeprecatedGameState private constructor(
    val players: Set<PlayerId>,
    val phase: GamePhase,
) {
    fun apply(event: GameEvent): GameStateResult =
        when (event) {
            is GameStartedEvent -> startGame(event)
            is RoundStartedEvent -> startRound(event)
        }

    private fun startGame(event: GameStartedEvent): GameStateResult =
        copy(
            players = event.players,
            phase = AwaitingNextRound,
        ).asSuccess()

    private fun startRound(event: RoundStartedEvent): GameStateResult =
        when {
            phase != AwaitingNextRound ->
                GameErrorCode
                    .CannotPerformActionInCurrentPhase(
                        command = StartRoundCommand,
                        phase = phase,
                    ).asFailure()

            else -> copy(phase = Bidding).asSuccess()
        }

    companion object {
        val new =
            DeprecatedGameState(
                players = emptySet(),
                phase = NotStarted,
            )
    }
}

sealed interface GamePhase {
    data object NotStarted : GamePhase

    data object AwaitingNextRound : GamePhase

    data object Bidding : GamePhase
}
