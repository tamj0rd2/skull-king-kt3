package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.GameHasAlreadyStarted
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.LobbyIsFull
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.PlayerHasAlreadyJoined
import com.tamj0rd2.skullking.domain.game.Lobby.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.Lobby.Companion.MINIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.StartGameErrorCode.TooFewPlayers
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.map

data class LobbyState private constructor(
    val atVersion: Version,
    val players: List<PlayerId>,
    val gameState: GameState?,
    val notifications: Notifications,
) {
    internal fun apply(event: LobbyEvent): Result4k<LobbyState, LobbyErrorCode> =
        copy(atVersion = atVersion.next()).run {
            when (event) {
                is LobbyCreatedEvent -> apply(event)
                is PlayerJoinedEvent -> apply(event)
                is GameStartedEvent -> apply(event)
                is CardDealtEvent -> withNotifications(LobbyNotification.ACardWasDealt(Card)).asSuccess()

                // TODO: this is pretty awful... I think I should have a separation between notifications about the lobby, and notifications
                //  about the game. even just to make things easier to derive.
                is BidPlacedEvent ->
                    updateGameState { it.apply(event) }.map {
                        it.withNotifications(
                            *buildList {
                                add(LobbyNotification.ABidWasPlaced(event.playerId))

                                if (it.gameState!!.allBidsHaveBeenPlaced) {
                                    add(LobbyNotification.AllBidsHaveBeenPlaced(it.gameState.bids))
                                }
                            }.toTypedArray(),
                        )
                    }
            }
        }

    private fun apply(event: LobbyCreatedEvent): Result4k<LobbyState, LobbyErrorCode> =
        copy(players = listOf(event.createdBy))
            .withNotifications(LobbyNotification.APlayerHasJoined(event.createdBy))
            .asSuccess()

    private fun apply(event: PlayerJoinedEvent): Result4k<LobbyState, AddPlayerErrorCode> {
        if (gameState != null) return GameHasAlreadyStarted().asFailure()
        if (players.size >= MAXIMUM_PLAYER_COUNT) return LobbyIsFull().asFailure()
        if (players.contains(event.playerId)) return PlayerHasAlreadyJoined().asFailure()
        return copy(players = players + event.playerId)
            .withNotifications(LobbyNotification.APlayerHasJoined(event.playerId))
            .asSuccess()
    }

    private fun apply(
        @Suppress("UNUSED_PARAMETER") event: GameStartedEvent,
    ): Result4k<LobbyState, StartGameErrorCode> {
        if (players.size < MINIMUM_PLAYER_COUNT) return TooFewPlayers().asFailure()
        return copy(gameState = GameState.new(players))
            .withNotifications(LobbyNotification.TheGameHasStarted)
            .asSuccess()
    }

    private fun updateGameState(block: (GameState) -> Result4k<GameState, LobbyErrorCode>): Result4k<LobbyState, LobbyErrorCode> {
        if (gameState == null) return GameNotInProgress().asFailure()
        return block(gameState).map { updatedGameState -> copy(gameState = updatedGameState) }
    }

    private fun withNotifications(vararg newNotifications: LobbyNotification) =
        copy(notifications = notifications.add(atVersion, newNotifications.toList()))

    companion object {
        internal fun new() =
            LobbyState(
                atVersion = Version.NONE,
                players = emptyList(),
                gameState = null,
                notifications = Notifications(emptyMap()),
            )
    }
}
