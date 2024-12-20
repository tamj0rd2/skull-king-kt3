package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.GameHasAlreadyStarted
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.GameIsFull
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.PlayerHasAlreadyJoined
import com.tamj0rd2.skullking.domain.game.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.Game.Companion.MINIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.StartGameErrorCode.TooFewPlayers
import com.tamj0rd2.skullking.domain.game.Status.IN_LOBBY
import com.tamj0rd2.skullking.domain.game.Status.IN_PROGRESS
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value

data class GameState private constructor(
    val players: List<PlayerId>,
    val status: Status,
) {
    internal fun apply(event: GameEvent): Result4k<GameState, GameErrorCode> =
        when (event) {
            is GameCreatedEvent -> apply(event)
            is PlayerJoinedEvent -> apply(event)
            is GameStartedEvent -> apply(event)
            is CardDealtEvent -> asSuccess()
        }

    private fun apply(event: GameCreatedEvent): Result4k<GameState, GameErrorCode> = copy(players = players + event.createdBy).asSuccess()

    private fun apply(event: PlayerJoinedEvent): Result4k<GameState, AddPlayerErrorCode> {
        if (status == IN_PROGRESS) return GameHasAlreadyStarted().asFailure()
        if (players.size >= MAXIMUM_PLAYER_COUNT) return GameIsFull().asFailure()
        if (players.contains(event.playerId)) return PlayerHasAlreadyJoined().asFailure()
        return copy(players = players + event.playerId).asSuccess()
    }

    private fun apply(
        @Suppress("UNUSED_PARAMETER") event: GameStartedEvent,
    ): Result4k<GameState, StartGameErrorCode> {
        if (players.size < MINIMUM_PLAYER_COUNT) return TooFewPlayers().asFailure()
        return copy(status = IN_PROGRESS).asSuccess()
    }

    companion object {
        internal fun new() =
            GameState(
                players = emptyList(),
                status = IN_LOBBY,
            )
    }
}

enum class Status {
    IN_LOBBY,
    IN_PROGRESS,
}

// TODO: the round number can never be greater than 10
// TODO: Also, make a value to present, No Round or something
@JvmInline
value class RoundNumber private constructor(
    override val value: Int,
) : Value<Int> {
    companion object : IntValueFactory<RoundNumber>(::RoundNumber) {
        val none = RoundNumber(0)
    }

    fun next() = RoundNumber.of(value + 1)
}

data object Card
