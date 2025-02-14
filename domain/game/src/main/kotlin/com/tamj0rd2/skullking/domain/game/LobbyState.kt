package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.GameHasAlreadyStarted
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.LobbyIsFull
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.PlayerHasAlreadyJoined
import com.tamj0rd2.skullking.domain.game.Lobby.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.Lobby.Companion.MINIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.LobbyNotification.ABidWasPlaced
import com.tamj0rd2.skullking.domain.game.LobbyNotification.ACardWasDealt
import com.tamj0rd2.skullking.domain.game.LobbyNotification.ACardWasPlayed
import com.tamj0rd2.skullking.domain.game.LobbyNotification.AllBidsHaveBeenPlaced
import com.tamj0rd2.skullking.domain.game.LobbyNotification.TheTrickHasEnded
import com.tamj0rd2.skullking.domain.game.StartGameErrorCode.TooFewPlayers
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.onFailure

data class LobbyState private constructor(
    val atVersion: Version,
    val players: List<PlayerId>,
    val gameState: GameState?,
    val notifications: List<LobbyNotification>,
) {
    internal fun apply(event: LobbyEvent): Result4k<LobbyState, LobbyErrorCode> =
        copy(atVersion = atVersion.next()).run {
            when (event) {
                is LobbyCreatedEvent -> apply(event)
                is PlayerJoinedEvent -> apply(event)
                is GameStartedEvent -> apply(event)
                is CardDealtEvent -> apply(event)
                is BidPlacedEvent -> apply(event)
                is CardPlayedEvent -> apply(event)
            }
        }

    private fun apply(event: LobbyCreatedEvent): Result4k<LobbyState, LobbyErrorCode> =
        copy(
            players = listOf(event.createdBy),
            notifications = listOf(LobbyNotification.APlayerHasJoined(event.createdBy)),
        ).asSuccess()

    private fun apply(event: PlayerJoinedEvent): Result4k<LobbyState, AddPlayerErrorCode> {
        if (gameState != null) return GameHasAlreadyStarted().asFailure()
        if (players.size >= MAXIMUM_PLAYER_COUNT) return LobbyIsFull().asFailure()
        if (players.contains(event.playerId)) return PlayerHasAlreadyJoined().asFailure()

        return copy(
            players = players + event.playerId,
            notifications = listOf(LobbyNotification.APlayerHasJoined(event.playerId)),
        ).asSuccess()
    }

    private fun apply(
        @Suppress("UNUSED_PARAMETER") event: GameStartedEvent,
    ): Result4k<LobbyState, StartGameErrorCode> {
        if (players.size < MINIMUM_PLAYER_COUNT) return TooFewPlayers().asFailure()

        return copy(
            gameState = GameState.new(players),
            notifications = listOf(LobbyNotification.TheGameHasStarted),
        ).asSuccess()
    }

    private fun apply(
        @Suppress("UNUSED_PARAMETER") event: CardDealtEvent,
    ) = copy(notifications = listOf(ACardWasDealt(Card))).asSuccess()

    private fun apply(event: BidPlacedEvent): Result<LobbyState, LobbyErrorCode> {
        if (gameState == null) return GameNotInProgress().asFailure()
        val updatedGameState = gameState.apply(event).onFailure { return it }
        val newNotifications =
            buildList {
                add(ABidWasPlaced(event.playerId))
                if (updatedGameState.allBidsHaveBeenPlaced) add(AllBidsHaveBeenPlaced(updatedGameState.bids))
            }

        return copy(
            gameState = updatedGameState,
            notifications = newNotifications,
        ).asSuccess()
    }

    private fun apply(event: CardPlayedEvent): Success<LobbyState> {
        val newNotifications =
            buildList {
                val playedCard = PlayedCard(event.card, event.playerId)
                add(ACardWasPlayed(playedCard))

                // FIXME: the winner is wrong. drive out correct behaviour through a test.
                add(TheTrickHasEnded(event.playerId))
            }

        return copy(notifications = newNotifications).asSuccess()
    }

    companion object {
        internal fun new() =
            LobbyState(
                atVersion = Version.NONE,
                players = emptyList(),
                gameState = null,
                notifications = emptyList(),
            )
    }
}
