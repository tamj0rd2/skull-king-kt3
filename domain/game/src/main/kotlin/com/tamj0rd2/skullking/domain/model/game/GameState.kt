package com.tamj0rd2.skullking.domain.model.game

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.StartGameErrorCode.TooFewPlayers
import dev.forkhandles.result4k.Result4k

data class GameState private constructor(
    val players: List<PlayerId>,
    val playerHands: Map<PlayerId, List<Card>>,
) {
    private fun apply(event: PlayerJoinedEvent): Result4k<GameState, AddPlayerErrorCode> {
        if (players.size >= Game.MAXIMUM_PLAYER_COUNT) return GameIsFull().asFailure()
        if (players.contains(event.playerId)) return PlayerHasAlreadyJoined().asFailure()
        return copy(players = players + event.playerId).asSuccess()
    }

    private fun apply(event: GameStartedEvent): Result4k<GameState, StartGameErrorCode> {
        if (players.size < Game.MINIMUM_PLAYER_COUNT) return TooFewPlayers().asFailure()
        return copy(playerHands = players.associateWith { listOf(Card) }).asSuccess()
    }

    internal fun apply(event: GameEvent): Result4k<GameState, GameErrorCode> =
        when (event) {
            is GameCreatedEvent -> this.asSuccess()
            is PlayerJoinedEvent -> apply(event)
            is GameStartedEvent -> apply(event)
        }

    companion object {
        internal fun new() =
            GameState(
                players = emptyList(),
                playerHands = emptyMap(),
            )
    }
}

data object Card
