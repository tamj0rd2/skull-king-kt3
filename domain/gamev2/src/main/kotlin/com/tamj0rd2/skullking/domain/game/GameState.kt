package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asSuccess
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
) {
    fun applyEvent(event: GameEvent): Result4k<GameState, GameErrorCode> =
        when (event) {
            is GameStarted ->
                copy(players = event.players).asSuccess()

            is RoundStarted,
            is RoundCompleted,
            is BidPlaced,
            is TrickStarted,
            is CardPlayed,
            is TrickCompleted,
            is GameCompleted,
            -> this.asSuccess()
        }

    companion object {
        val new =
            GameState(
                players = emptySet(),
            )
    }
}
