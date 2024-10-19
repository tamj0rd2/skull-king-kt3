package com.tamj0rd2.skullking.domain.model.game

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.game.Game.Companion.MINIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.game.StartGameErrorCode.TooFewPlayers
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value

data class GameState private constructor(
    val players: List<PlayerId>,
) {
    internal fun apply(event: GameEvent): Result4k<GameState, GameErrorCode> =
        when (event) {
            is GameCreatedEvent -> asSuccess()
            is PlayerJoinedEvent -> apply(event)
            is GameStartedEvent -> apply(event)
            is CardDealtEvent -> asSuccess()
        }

    private fun apply(event: PlayerJoinedEvent): Result4k<GameState, AddPlayerErrorCode> {
        if (players.size >= MAXIMUM_PLAYER_COUNT) return GameIsFull().asFailure()
        if (players.contains(event.playerId)) return PlayerHasAlreadyJoined().asFailure()
        return copy(players = players + event.playerId).asSuccess()
    }

    private fun apply(
        @Suppress("UNUSED_PARAMETER") event: GameStartedEvent,
    ): Result4k<GameState, StartGameErrorCode> {
        if (players.size < MINIMUM_PLAYER_COUNT) return TooFewPlayers().asFailure()
        return this.asSuccess()
    }

    companion object {
        internal fun new() =
            GameState(
                players = emptyList(),
            )
    }
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
