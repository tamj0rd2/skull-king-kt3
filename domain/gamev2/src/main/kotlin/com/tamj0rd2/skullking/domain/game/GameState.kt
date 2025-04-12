package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
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
import dev.forkhandles.result4k.Result4k

data class GameState private constructor(
    val players: Set<PlayerId>,
    val roundNumber: RoundNumber,
    val roundIsInProgress: Boolean,
) {
    fun applyEvent(event: GameEvent): Result4k<GameState, GameErrorCode> =
        when (event) {
            is GameStarted ->
                copy(players = event.players).asSuccess()

            is RoundStarted -> applyEvent(event)
            is RoundCompleted -> applyEvent(event)
            is BidPlaced,
            is TrickStarted,
            is CardPlayed,
            is TrickCompleted,
            is GameCompleted,
            -> this.asSuccess()
        }

    // TODO: this logic could ready more nicely.
    private fun applyEvent(event: RoundStarted): Result4k<GameState, GameErrorCode> =
        when {
            event.roundNumber > RoundNumber.last -> CannotPlayMoreThan10Rounds().asFailure()
            event.roundNumber < roundNumber -> CannotStartAPreviousRound().asFailure()
            event.roundNumber > roundNumber.next -> CannotStartARoundMoreThan1Ahead().asFailure()
            roundIsInProgress -> CannotStartARoundThatIsAlreadyInProgress().asFailure()
            else -> copy(roundNumber = event.roundNumber, roundIsInProgress = true).asSuccess()
        }

    private fun applyEvent(event: RoundCompleted): Result4k<GameState, GameErrorCode> =
        when {
            !roundIsInProgress -> CannotCompleteARoundThatIsNotInProgress().asFailure()

            else -> copy().asSuccess()
        }

    companion object {
        val new =
            GameState(
                players = emptySet(),
                roundNumber = RoundNumber.none,
                roundIsInProgress = false,
            )
    }
}
