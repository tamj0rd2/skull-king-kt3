package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotBidOutsideBiddingPhase
import dev.forkhandles.result4k.Result4k

sealed class GamePhase {
    internal abstract fun transition(event: GameEvent): Result4k<GamePhase, GameErrorCode>

    data object None : GamePhase() {
        override fun transition(event: GameEvent): Result4k<GamePhase, GameErrorCode> =
            when (event) {
                is GameEvent.GameStarted -> AwaitingNextRound.asSuccess()
                else -> this.asSuccess()
            }
    }

    data object AwaitingNextRound : GamePhase() {
        override fun transition(event: GameEvent): Result4k<GamePhase, GameErrorCode> =
            when (event) {
                is GameEvent.BidPlaced -> CannotBidOutsideBiddingPhase.asFailure()
                else -> this.asSuccess()
            }
    }

    data object Bidding : GamePhase() {
        override fun transition(event: GameEvent): Result4k<GamePhase, GameErrorCode> =
            when (event) {
                else -> this.asSuccess()
            }
    }
}
