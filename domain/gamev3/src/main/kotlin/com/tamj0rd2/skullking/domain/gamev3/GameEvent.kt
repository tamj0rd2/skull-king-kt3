package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import dev.forkhandles.result4k.Result

sealed interface GameEvent {
    val id: GameId
}

data class GameStartedEvent private constructor(
    override val id: GameId,
    // TODO: should be SomePlayerId
    val players: Set<PlayerId>,
) : GameEvent {
    companion object {
        fun new(
            id: GameId,
            players: Set<PlayerId>,
        ): Result<GameStartedEvent, GameErrorCode> =
            when {
                players.size < 2 -> GameErrorCode.NotEnoughPlayers.asFailure()
                players.size > 6 -> GameErrorCode.TooManyPlayers.asFailure()
                else -> GameStartedEvent(id, players).asSuccess()
            }
    }
}

data class RoundStartedEvent(
    override val id: GameId,
) : GameEvent

data class BidPlacedEvent(
    override val id: GameId,
    val playerId: PlayerId,
    val bid: SomeBid,
) : GameEvent
