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

data class GameState private constructor(
    val players: List<PlayerId>,
    val status: Status,
    val bids: Map<PlayerId, Bid>,
) {
    val allBidsHaveBeenPlaced = bids.keys == players.toSet()

    internal fun apply(event: GameEvent): Result4k<GameState, GameErrorCode> =
        when (event) {
            is GameCreatedEvent -> apply(event)
            is PlayerJoinedEvent -> apply(event)
            is GameStartedEvent -> apply(event)
            is CardDealtEvent -> asSuccess()
            is BidPlacedEvent -> apply(event)
        }

    private fun apply(event: GameCreatedEvent): Result4k<GameState, GameErrorCode> = copy(players = listOf(event.createdBy)).asSuccess()

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

    private fun apply(event: BidPlacedEvent): Result4k<GameState, AddPlayerErrorCode> =
        copy(bids = bids + Pair(event.playerId, event.bid)).asSuccess()

    companion object {
        internal fun new() =
            GameState(
                players = emptyList(),
                status = IN_LOBBY,
                bids = emptyMap(),
            )
    }
}

enum class Status {
    IN_LOBBY,
    IN_PROGRESS,
}

data object Card
