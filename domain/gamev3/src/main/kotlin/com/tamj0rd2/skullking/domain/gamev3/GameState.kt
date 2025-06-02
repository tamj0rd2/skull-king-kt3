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
                is GameStartedEvent -> return AwaitingNextRound(
                    players = event.players,
                    roundNumber = RoundNumber.One,
                ).asSuccess()

                is BidPlacedEvent,
                is RoundStartedEvent,
                -> GameErrorCode.CannotApplyEventInCurrentState(event, this).asFailure()
            }
        }
    }

    sealed class InProgress : GameState() {
        abstract val players: Set<PlayerId>
        abstract val roundNumber: RoundNumber
    }

    data class AwaitingNextRound(
        override val players: Set<PlayerId>,
        override val roundNumber: RoundNumber,
    ) : InProgress() {
        override fun apply(event: GameEvent): GameStateResult {
            return when (event) {
                is RoundStartedEvent -> return Bidding(players, roundNumber).asSuccess()
                is BidPlacedEvent,
                is GameStartedEvent,
                -> GameErrorCode.CannotApplyEventInCurrentState(event, this).asFailure()
            }
        }
    }

    data class Bidding(
        override val players: Set<PlayerId>,
        override val roundNumber: RoundNumber,
        val bids: Map<PlayerId, Bid> = players.associateWith { NoBid },
    ) : InProgress() {
        override fun apply(event: GameEvent): GameStateResult =
            when (event) {
                is BidPlacedEvent -> addPlayerBid(event)
                is GameStartedEvent,
                is RoundStartedEvent,
                -> GameErrorCode.CannotApplyEventInCurrentState(event, this).asFailure()
            }

        private fun addPlayerBid(event: BidPlacedEvent): GameStateResult = copy(bids = bids + (event.playerId to event.bid)).asSuccess()
    }
}
