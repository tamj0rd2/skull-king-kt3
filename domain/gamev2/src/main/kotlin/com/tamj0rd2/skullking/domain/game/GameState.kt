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
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickStarted
import dev.forkhandles.result4k.Result4k

data class GameState private constructor(
    val players: Set<PlayerId>,
    val roundNumber: RoundNumber,
    val cardsPerPlayer: CardsPerPlayer,
) {
    fun applyEvent(event: GameEvent): Result4k<GameState, GameErrorCode> {
        when (event) {
            is BidPlaced -> {
                return this.asSuccess()
            }
            is CardPlayed -> {
                return this.asSuccess()
            }
            is GameCompleted -> {
                return NotImplemented().asFailure()
            }
            is GameStarted -> {
                if (event.players.size < 2) return NotEnoughPlayersToStartGame().asFailure()
                return copy(players = event.players).asSuccess()
            }
            is RoundCompleted -> {
                return NotImplemented().asFailure()
            }
            is RoundStarted -> {
                return copy(cardsPerPlayer = event.dealtCards).asSuccess()
            }
            is TrickCompleted -> {
                return this.asSuccess()
            }
            is TrickStarted -> {
                return this.asSuccess()
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
