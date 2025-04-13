package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.game.GameErrorCode.AlreadyBid
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotBidWhenRoundIsNotInProgress
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotCompleteARoundThatIsNotInProgress
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotPlayMoreThan10Rounds
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotStartAPreviousRound
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotStartARoundMoreThan1Ahead
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotStartARoundThatIsAlreadyInProgress
import com.tamj0rd2.skullking.domain.game.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.game.GameEvent.CardPlayed
import com.tamj0rd2.skullking.domain.game.GameEvent.GameCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.GameStarted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickStarted
import com.tamj0rd2.skullking.domain.game.GamePhase.AwaitingNextRound
import com.tamj0rd2.skullking.domain.game.values.Bid
import com.tamj0rd2.skullking.domain.game.values.RoundNumber
import dev.forkhandles.result4k.Result4k

data class GameState private constructor(
    val players: Set<PlayerId>,
    val roundNumber: RoundNumber,
    val bids: Map<PlayerId, RoundBid>,
    val phase: GamePhase,
) {
    val roundIsInProgress: Boolean = phase != AwaitingNextRound

    fun applyEvent(event: GameEvent): Result4k<GameState, GameErrorCode> =
        when (event) {
            is GameStarted ->
                copy(players = event.players).asSuccess()

            is RoundStarted -> applyEvent(event)
            is RoundCompleted -> applyEvent(event)
            is BidPlaced -> applyEvent(event)
            is TrickStarted,
            is CardPlayed,
            is TrickCompleted,
            is GameCompleted,
            -> this.asSuccess()
        }

    private fun applyEvent(event: RoundStarted): Result4k<GameState, GameErrorCode> =
        when {
            event.roundNumber > RoundNumber.last ->
                CannotPlayMoreThan10Rounds().asFailure()

            event.roundNumber < roundNumber ->
                CannotStartAPreviousRound().asFailure()

            event.roundNumber > roundNumber.next ->
                CannotStartARoundMoreThan1Ahead().asFailure()

            roundIsInProgress ->
                CannotStartARoundThatIsAlreadyInProgress().asFailure()

            else ->
                copy(
                    roundNumber = event.roundNumber,
                    phase = GamePhase.Bidding,
                    bids = players.associateWith { OutstandingBid },
                ).asSuccess()
        }

    private fun applyEvent(event: RoundCompleted): Result4k<GameState, GameErrorCode> =
        when {
            !roundIsInProgress -> CannotCompleteARoundThatIsNotInProgress().asFailure()

            else -> copy().asSuccess()
        }

    private fun applyEvent(event: BidPlaced): Result4k<GameState, GameErrorCode> =
        when {
            !roundIsInProgress ->
                CannotBidWhenRoundIsNotInProgress().asFailure()

            bids[event.placedBy] is APlacedBid ->
                AlreadyBid().asFailure()

            else ->
                copy(
                    bids = bids + Pair(event.placedBy, APlacedBid(event.bid)),
                ).asSuccess()
        }

    companion object {
        val new =
            GameState(
                players = emptySet(),
                roundNumber = RoundNumber.none,
                bids = emptyMap(),
                phase = AwaitingNextRound,
            )
    }
}

sealed interface RoundBid

data class APlacedBid(
    val value: Bid,
) : RoundBid

data object OutstandingBid : RoundBid
