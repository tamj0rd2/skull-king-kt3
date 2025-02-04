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
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.map

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

    private fun apply(
        @Suppress("UNUSED_PARAMETER") event: CardDealtEvent,
    ) = withNotification(ACardWasDealt(Card)).asSuccess()

    private fun apply(event: BidPlacedEvent) =
        updateGameState { it.apply(event) }.map { gameState ->
            gameState.withNotifications(
                buildList {
                    add(ABidWasPlaced(event.playerId))

                    if (gameState.gameState!!.allBidsHaveBeenPlaced) {
                        add(AllBidsHaveBeenPlaced(gameState.gameState.bids))
                    }
                },
            )
        }

    private fun apply(event: CardPlayedEvent) =
        withNotifications(
            buildList {
                val playedCard = PlayedCard(event.card, event.playerId)
                add(ACardWasPlayed(playedCard))

                // FIXME: the winner is wrong. drive out correct behaviour through a test.
                add(TheTrickHasEnded(event.playerId))
            },
        ).asSuccess()

    private fun updateGameState(block: (GameState) -> Result4k<GameState, LobbyErrorCode>): Result4k<LobbyState, LobbyErrorCode> {
        if (gameState == null) return GameNotInProgress().asFailure()
        return block(gameState).map { updatedGameState -> copy(gameState = updatedGameState) }
    }

    private fun withNotifications(newNotifications: List<LobbyNotification>) = copy(notifications = newNotifications)

    private fun withNotification(newNotification: LobbyNotification) = withNotifications(listOf(newNotification))

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
