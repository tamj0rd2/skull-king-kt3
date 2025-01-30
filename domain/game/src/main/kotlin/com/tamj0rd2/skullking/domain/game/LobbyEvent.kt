package com.tamj0rd2.skullking.domain.game

sealed interface LobbyEvent {
    val lobbyId: LobbyId
}

data class LobbyCreatedEvent(
    override val lobbyId: LobbyId,
    val createdBy: PlayerId,
) : LobbyEvent

data class PlayerJoinedEvent(
    override val lobbyId: LobbyId,
    val playerId: PlayerId,
) : LobbyEvent

data class GameStartedEvent(
    override val lobbyId: LobbyId,
) : LobbyEvent

data class CardDealtEvent(
    override val lobbyId: LobbyId,
) : LobbyEvent

data class BidPlacedEvent(
    override val lobbyId: LobbyId,
    val playerId: PlayerId,
    val bid: Bid,
) : LobbyEvent

data class CardPlayedEvent(
    override val lobbyId: LobbyId,
    val playerId: PlayerId,
    val card: Card,
) : LobbyEvent
