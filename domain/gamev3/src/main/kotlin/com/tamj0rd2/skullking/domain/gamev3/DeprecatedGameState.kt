package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.gamev3.GameState.AwaitingNextRound
import com.tamj0rd2.skullking.domain.gamev3.GameState.Bidding
import com.tamj0rd2.skullking.domain.gamev3.GameState.NotStarted
import dev.forkhandles.result4k.Result4k

typealias DeprecatedGameStateResult = Result4k<DeprecatedGameState, GameErrorCode>

data class DeprecatedGameState private constructor(
    val players: Set<PlayerId>,
    val state: GameState,
) {
    fun apply(event: GameEvent): DeprecatedGameStateResult =
        when (event) {
            is GameStartedEvent -> startGame(event)
            is RoundStartedEvent -> startRound(event)
        }

    private fun startGame(event: GameStartedEvent): DeprecatedGameStateResult =
        copy(
            players = event.players,
            state = AwaitingNextRound(event.players),
        ).asSuccess()

    private fun startRound(event: RoundStartedEvent): DeprecatedGameStateResult =
        when {
            state !is AwaitingNextRound ->
                GameErrorCode
                    .CannotApplyEventInCurrentState(
                        event = event,
                        phase = state,
                    ).asFailure()

            else -> copy(state = Bidding(players)).asSuccess()
        }

    companion object {
        val new =
            DeprecatedGameState(
                players = emptySet(),
                state = NotStarted,
            )
    }
}
