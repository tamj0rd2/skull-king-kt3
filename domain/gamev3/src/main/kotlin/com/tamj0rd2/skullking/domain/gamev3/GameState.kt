package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.gamev3.GameErrorCode.CannotApplyEventInCurrentState
import com.tamj0rd2.skullking.domain.gamev3.GameStateName.AwaitingNextRound
import com.tamj0rd2.skullking.domain.gamev3.PlayedCard.Companion.playedBy
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success

typealias GameStateResult = Result4k<GameState, GameErrorCode>

enum class GameStateName {
    NotStarted,
    AwaitingNextRound,
    Bidding,
    TrickTaking,
}

sealed class GameState {
    internal abstract val name: GameStateName

    abstract fun apply(event: GameEvent): GameStateResult

    data object NotStarted : GameState() {
        override val name = GameStateName.NotStarted

        override fun apply(event: GameEvent): GameStateResult {
            return when (event) {
                is GameStartedEvent -> return AwaitingNextRound(
                    players = event.players,
                    roundNumber = RoundNumber.One,
                ).asSuccess()

                else -> CannotApplyEventInCurrentState(this, event).asFailure()
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
        override val name = AwaitingNextRound

        override fun apply(event: GameEvent): GameStateResult {
            return when (event) {
                is RoundStartedEvent -> return Bidding(players, roundNumber).asSuccess()
                else -> CannotApplyEventInCurrentState(this, event).asFailure()
            }
        }
    }

    data class Bidding(
        override val players: Set<PlayerId>,
        override val roundNumber: RoundNumber,
        val bids: Map<PlayerId, Bid> = players.associateWith { NoBid },
    ) : InProgress() {
        override val name = GameStateName.Bidding

        override fun apply(event: GameEvent): GameStateResult =
            when (event) {
                is BidPlacedEvent -> addPlayerBid(event)
                is TrickStartedEvent -> startTrick()
                else -> CannotApplyEventInCurrentState(this, event).asFailure()
            }

        private fun startTrick(): GameStateResult =
            TrickTaking(
                players = players,
                roundNumber = roundNumber,
                trickNumber = TrickNumber.One,
            ).asSuccess()

        private fun addPlayerBid(event: BidPlacedEvent): GameStateResult {
            when {
                event.playerId !in players -> return GameErrorCode.PlayerNotInTheGame.asFailure()
            }

            return copy(bids = bids + (event.playerId to event.bid)).asSuccess()
        }
    }

    data class TrickTaking(
        override val players: Set<PlayerId>,
        override val roundNumber: RoundNumber,
        val trickNumber: TrickNumber,
        val playedCards: List<PlayedCard> = emptyList(),
    ) : InProgress() {
        override val name = GameStateName.TrickTaking

        override fun apply(event: GameEvent): GameStateResult =
            when (event) {
                is CardPlayedEvent -> addPlayedCard(event)
                else -> CannotApplyEventInCurrentState(this, event).asFailure()
            }

        private fun addPlayedCard(event: CardPlayedEvent): Success<TrickTaking> =
            copy(playedCards = playedCards + event.card.playedBy(event.playerId)).asSuccess()
    }
}
