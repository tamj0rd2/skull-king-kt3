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
    val events: List<GameEvent>,
) {
    fun applyEvent(event: GameEvent): Result4k<GameState, GameErrorCode> =
        copy(events = events + event).run {
            when (event) {
                is GameStarted ->
                    copy(players = event.players).asSuccess()

                is RoundStarted -> {
                    require(events.count { it is RoundStarted } < 10) { "already started 10 or more rounds" }
                    require(events.count { it is RoundCompleted } < 10) { "already completed 10 or more rounds" }

                    this.asSuccess()
                }

                is BidPlaced,
                is TrickStarted,
                is CardPlayed,
                is TrickCompleted,
                is RoundCompleted,
                is GameCompleted,
                -> this.asSuccess()
            }
        }

    companion object {
        val new =
            GameState(
                players = emptySet(),
                events = emptyList(),
            )
    }
}
