package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import dev.forkhandles.result4k.Result4k

typealias GameStateResult = Result4k<GameState, GameErrorCode>

sealed class GameState {
    // TODO: how can I make sure this is only ever called from the Game aggregate?
    internal abstract fun apply(event: GameEvent): GameStateResult

    data object NotStarted : GameState() {
        override fun apply(event: GameEvent): GameStateResult {
            return when (event) {
                is GameStartedEvent -> return AwaitingNextRound.asSuccess()
                else -> GameErrorCode.CannotApplyEventInCurrentState(event, this).asFailure()
            }
        }
    }

    data object AwaitingNextRound : GameState() {
        override fun apply(event: GameEvent): GameStateResult {
            return when (event) {
                is RoundStartedEvent -> return Bidding.asSuccess()
                else -> GameErrorCode.CannotApplyEventInCurrentState(event, this).asFailure()
            }
        }
    }

    data object Bidding : GameState() {
        override fun apply(event: GameEvent): GameStateResult = GameErrorCode.CannotApplyEventInCurrentState(event, this).asFailure()
    }
}
