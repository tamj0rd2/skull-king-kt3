package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.Event

sealed interface LobbyEvent : Event<LobbyId> {
    override val entityId: LobbyId
}

data class LobbyCreatedEvent(
    override val entityId: LobbyId,
    val createdBy: PlayerId,
) : LobbyEvent

data class PlayerJoinedEvent(
    override val entityId: LobbyId,
    val playerId: PlayerId,
) : LobbyEvent

data class GameStartedEvent(
    override val entityId: LobbyId,
) : LobbyEvent

data class CardDealtEvent(
    override val entityId: LobbyId,
) : LobbyEvent

data class BidPlacedEvent(
    override val entityId: LobbyId,
    val playerId: PlayerId,
    val bid: Bid,
) : LobbyEvent

data class CardPlayedEvent(
    override val entityId: LobbyId,
    val playerId: PlayerId,
    val card: Card,
) : LobbyEvent
