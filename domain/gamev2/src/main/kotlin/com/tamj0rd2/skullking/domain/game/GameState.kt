package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.game.GameErrorCode.NotEnoughPlayersToStartGame
import com.tamj0rd2.skullking.domain.game.GameErrorCode.NotImplemented
import com.tamj0rd2.skullking.domain.game.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.game.GameEvent.CardPlayed
import com.tamj0rd2.skullking.domain.game.GameEvent.GameCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.GameStarted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickCompleted
import dev.forkhandles.result4k.Result4k

data class GameState private constructor(
    val players: Set<PlayerId>,
    val roundNumber: RoundNumber,
    val cardsPerPlayer: CardsPerPlayer,
) {
    fun applyEvent(event: GameEvent): Result4k<GameState, GameErrorCode> {
        return when (event) {
            is BidPlaced -> {
                NotImplemented().asFailure()
            }
            is CardPlayed -> {
                NotImplemented().asFailure()
            }
            is GameCompleted -> {
                NotImplemented().asFailure()
            }
            is GameStarted -> {
                if (event.players.size < 2) return NotEnoughPlayersToStartGame().asFailure()
                return copy(players = event.players).asSuccess()
            }
            is RoundCompleted -> {
                NotImplemented().asFailure()
            }
            is RoundStarted -> {
                return copy(cardsPerPlayer = event.dealtCards).asSuccess()
            }
            is TrickCompleted -> {
                NotImplemented().asFailure()
            }
        }
    }

    companion object {
        val new =
            GameState(
                players = emptySet(),
                roundNumber = RoundNumber.none,
                cardsPerPlayer = CardsPerPlayer(emptyMap()),
            )
    }
}
