package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.AlreadyBid
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.CannotBidOutsideBiddingPhase
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.CannotCompleteRoundFromCurrentPhase
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.CannotPlayMoreThan10Rounds
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.CannotStartAPreviousRound
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.CannotStartARoundMoreThan1Ahead
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.CannotStartARoundThatIsAlreadyInProgress
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.CannotStartATrickFromCurrentPhase
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.CardPlayed
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.GameCompleted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.GameStarted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.TrickCompleted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.TrickStarted
import com.tamj0rd2.skullking.domain.gamev2.GamePhase.AwaitingNextRound
import com.tamj0rd2.skullking.domain.gamev2.GamePhase.Bidding
import com.tamj0rd2.skullking.domain.gamev2.GamePhase.None
import com.tamj0rd2.skullking.domain.gamev2.GamePhase.TrickScoring
import com.tamj0rd2.skullking.domain.gamev2.GamePhase.TrickTaking
import com.tamj0rd2.skullking.domain.gamev2.values.Bid
import com.tamj0rd2.skullking.domain.gamev2.values.RoundNumber
import dev.forkhandles.result4k.Result4k

sealed interface Round {
    data class InProgress(
        val roundNumber: RoundNumber,
        val bids: Map<PlayerId, RoundBid>,
    ) : Round

    data class WaitingToStart(
        val roundNumber: RoundNumber,
    ) : Round
}

typealias GameStateResult = Result4k<GameState, GameErrorCode>

data class GameState private constructor(
    val players: Set<PlayerId>,
    val round: Round,
    val phase: GamePhase,
) {
    val roundNumberInProgress =
        when (round) {
            is Round.InProgress -> round.roundNumber
            is Round.WaitingToStart -> null
        }

    fun applyEvent(event: GameEvent): GameStateResult =
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

    private fun applyEvent(event: GameStarted): GameStateResult =
        when {
            else ->
                copy(
                    phase = AwaitingNextRound,
                    players = event.players,
                ).asSuccess()
        }

    private fun applyEvent(event: RoundStarted): GameStateResult =
        when {
            event.roundNumber > RoundNumber.Ten ->
                CannotPlayMoreThan10Rounds.asFailure()

            round is Round.InProgress ->
                CannotStartARoundThatIsAlreadyInProgress.asFailure()

            round !is Round.WaitingToStart -> TODO()

            event.roundNumber < round.roundNumber ->
                CannotStartAPreviousRound.asFailure()

            event.roundNumber > round.roundNumber.next() ->
                CannotStartARoundMoreThan1Ahead.asFailure()

            else ->
                copy(
                    phase = Bidding,
                    round =
                        Round.InProgress(
                            roundNumber = event.roundNumber,
                            bids = players.associateWith { OutstandingBid },
                        ),
                ).asSuccess()
        }

    private fun applyEvent(
        @Suppress("UNUSED_PARAMETER") event: RoundCompleted,
    ): GameStateResult =
        when {
            phase != TrickScoring ->
                CannotCompleteRoundFromCurrentPhase(phase).asFailure()

            else ->
                copy(
                    phase = AwaitingNextRound,
                ).asSuccess()
        }

    private fun applyEvent(event: BidPlaced): GameStateResult =
        when {
            phase != Bidding ->
                CannotBidOutsideBiddingPhase.asFailure()

            round !is Round.InProgress -> TODO("what do?")

            round.bids[event.placedBy] is APlacedBid ->
                AlreadyBid.asFailure()

            else ->
                copy(
                    // TODO: eeeewwww
                    round =
                        round.copy(
                            bids = round.bids + Pair(event.placedBy, APlacedBid(event.bid)),
                        ),
                ).asSuccess()
        }

    private fun applyEvent(
        @Suppress("UNUSED_PARAMETER") event: TrickStarted,
    ): GameStateResult =
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
                round = Round.WaitingToStart(RoundNumber.One),
                phase = None,
            )
    }
}

sealed interface RoundBid

data class APlacedBid(
    val value: Bid,
) : RoundBid

data object OutstandingBid : RoundBid
