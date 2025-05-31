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
                is GameStartedEvent -> return AwaitingNextRound(event.players).asSuccess()
                else -> GameErrorCode.CannotApplyEventInCurrentState(event, this).asFailure()
            }
        }
    }

    sealed class InProgress : GameState() {
        abstract val players: Set<PlayerId>
    }

    data class AwaitingNextRound(
        override val players: Set<PlayerId>,
    ) : InProgress() {
        override fun apply(event: GameEvent): GameStateResult {
            return when (event) {
                is RoundStartedEvent -> return Bidding(players).asSuccess()
                else -> GameErrorCode.CannotApplyEventInCurrentState(event, this).asFailure()
            }
        }
    }

    data class Bidding(
        override val players: Set<PlayerId>,
    ) : InProgress() {
        override fun apply(event: GameEvent): GameStateResult = GameErrorCode.CannotApplyEventInCurrentState(event, this).asFailure()
    }
}
