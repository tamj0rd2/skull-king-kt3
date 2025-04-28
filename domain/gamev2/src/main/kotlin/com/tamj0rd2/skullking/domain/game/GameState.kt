package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.game.GameErrorCode.AlreadyBid
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotBidOutsideBiddingPhase
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotCompleteRoundFromCurrentPhase
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotPlayMoreThan10Rounds
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotStartAPreviousRound
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotStartARoundMoreThan1Ahead
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotStartARoundThatIsAlreadyInProgress
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotStartATrickFromCurrentPhase
import com.tamj0rd2.skullking.domain.game.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.game.GameEvent.CardPlayed
import com.tamj0rd2.skullking.domain.game.GameEvent.GameCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.GameStarted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickStarted
import com.tamj0rd2.skullking.domain.game.GamePhase.AwaitingNextRound
import com.tamj0rd2.skullking.domain.game.GamePhase.Bidding
import com.tamj0rd2.skullking.domain.game.GamePhase.None
import com.tamj0rd2.skullking.domain.game.GamePhase.TrickScoring
import com.tamj0rd2.skullking.domain.game.GamePhase.TrickTaking
import com.tamj0rd2.skullking.domain.game.values.Bid
import com.tamj0rd2.skullking.domain.game.values.RoundNumber
import dev.forkhandles.result4k.Result4k

data class RoundInProgress(
    val roundNumber: RoundNumber,
    val bids: Map<PlayerId, RoundBid>,
)

data class GameState private constructor(
    val players: Set<PlayerId>,
    val roundInProgress: RoundInProgress,
    val phase: GamePhase,
) {
    fun applyEvent(event: GameEvent): Result4k<GameState, GameErrorCode> =
        when (event) {
            // TODO: try naming these functions more specifically for sanity.
            is GameStarted -> applyEvent(event)
            is RoundStarted -> applyEvent(event)
            is RoundCompleted -> applyEvent(event)
            is BidPlaced -> applyEvent(event)
            is TrickStarted -> applyEvent(event)
            is CardPlayed,
            is TrickCompleted,
            -> copy(phase = TrickScoring).asSuccess()
            is GameCompleted,
            -> this.asSuccess()
        }

    private fun applyEvent(event: GameStarted): Result4k<GameState, GameErrorCode> =
        when {
            else ->
                copy(
                    phase = AwaitingNextRound,
                    players = event.players,
                ).asSuccess()
        }

    private fun applyEvent(event: RoundStarted): Result4k<GameState, GameErrorCode> =
        when {
            event.roundNumber > RoundNumber.last ->
                CannotPlayMoreThan10Rounds.asFailure()

            event.roundNumber < roundInProgress.roundNumber ->
                CannotStartAPreviousRound.asFailure()

            event.roundNumber > roundInProgress.roundNumber.next ->
                CannotStartARoundMoreThan1Ahead.asFailure()

            // TODO: this is smelly
            phase in setOf(Bidding, TrickTaking, TrickScoring) ->
                CannotStartARoundThatIsAlreadyInProgress.asFailure()

            else ->
                copy(
                    phase = Bidding,
                    roundInProgress =
                        RoundInProgress(
                            roundNumber = event.roundNumber,
                            bids = players.associateWith { OutstandingBid },
                        ),
                ).asSuccess()
        }

    private fun applyEvent(
        @Suppress("UNUSED_PARAMETER") event: RoundCompleted,
    ): Result4k<GameState, GameErrorCode> =
        when {
            phase != TrickScoring ->
                CannotCompleteRoundFromCurrentPhase(phase).asFailure()

            else -> copy().asSuccess()
        }

    private fun applyEvent(event: BidPlaced): Result4k<GameState, GameErrorCode> =
        when {
            phase != Bidding ->
                CannotBidOutsideBiddingPhase.asFailure()

            roundInProgress.bids[event.placedBy] is APlacedBid ->
                AlreadyBid.asFailure()

            else ->
                copy(
                    // TODO: eeeewwww
                    roundInProgress =
                        roundInProgress.copy(
                            bids = roundInProgress.bids + Pair(event.placedBy, APlacedBid(event.bid)),
                        ),
                ).asSuccess()
        }

    private fun applyEvent(
        @Suppress("UNUSED_PARAMETER") event: TrickStarted,
    ): Result4k<GameState, GameErrorCode> =
        when {
            phase !in setOf(Bidding, TrickScoring) -> CannotStartATrickFromCurrentPhase(phase).asFailure()

            else ->
                copy(
                    phase = TrickTaking,
                ).asSuccess()
        }

    companion object {
        val new =
            GameState(
                players = emptySet(),
                roundInProgress =
                    RoundInProgress(
                        roundNumber = RoundNumber.none,
                        bids = emptyMap(),
                    ),
                phase = None,
            )
    }
}

sealed interface RoundBid

data class APlacedBid(
    val value: Bid,
) : RoundBid

data object OutstandingBid : RoundBid
