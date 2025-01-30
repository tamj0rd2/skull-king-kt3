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
import com.tamj0rd2.skullking.domain.game.LobbyNotification.AllBidsHaveBeenPlaced
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
                is CardDealtEvent -> withNotification(ACardWasDealt(Card)).asSuccess()

                // TODO: this is pretty awful... I think I should have a separation between notifications about the lobby, and notifications
                //  about the game. even just to make things easier to derive.
                is BidPlacedEvent ->
                    updateGameState { it.apply(event) }.map {
                        it.withNotifications(
                            buildList {
                                add(ABidWasPlaced(event.playerId))

                                if (it.gameState!!.allBidsHaveBeenPlaced) {
                                    add(AllBidsHaveBeenPlaced(it.gameState.bids))
                                }
                            },
                        )
                    }

                is CardPlayedEvent ->
                    withNotifications(
                        buildList {
                            val playedCard = PlayedCard(event.card, event.playerId)
                            add(LobbyNotification.ACardWasPlayed(playedCard))

                            // FIXME: the winner is wrong. drive out correct behaviour through a test.
                            add(LobbyNotification.TheTrickHasEnded(event.playerId))
                        },
                    ).asSuccess()
            }
        }

    private fun apply(event: LobbyCreatedEvent): Result4k<LobbyState, LobbyErrorCode> =
        copy(players = listOf(event.createdBy))
            .withNotification(LobbyNotification.APlayerHasJoined(event.createdBy))
            .asSuccess()

    private fun apply(event: PlayerJoinedEvent): Result4k<LobbyState, AddPlayerErrorCode> {
        if (gameState != null) return GameHasAlreadyStarted().asFailure()
        if (players.size >= MAXIMUM_PLAYER_COUNT) return LobbyIsFull().asFailure()
        if (players.contains(event.playerId)) return PlayerHasAlreadyJoined().asFailure()
        return copy(players = players + event.playerId)
            .withNotification(LobbyNotification.APlayerHasJoined(event.playerId))
            .asSuccess()
    }

    private fun apply(
        @Suppress("UNUSED_PARAMETER") event: GameStartedEvent,
    ): Result4k<LobbyState, StartGameErrorCode> {
        if (players.size < MINIMUM_PLAYER_COUNT) return TooFewPlayers().asFailure()
        return copy(gameState = GameState.new(players))
            .withNotification(LobbyNotification.TheGameHasStarted)
            .asSuccess()
    }

    private fun updateGameState(block: (GameState) -> Result4k<GameState, LobbyErrorCode>): Result4k<LobbyState, LobbyErrorCode> {
        if (gameState == null) return GameNotInProgress().asFailure()
        return block(gameState).map { updatedGameState -> copy(gameState = updatedGameState) }
    }

    private fun withNotifications(newNotifications: List<LobbyNotification>) =
        copy(notifications = notifications.add(atVersion, newNotifications.toList()))

    private fun withNotification(newNotification: LobbyNotification) = withNotifications(listOf(newNotification))

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
