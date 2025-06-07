package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.Event

sealed interface LobbyEvent : Event<LobbyId> {
    override val aggregateId: LobbyId
}

data class LobbyCreatedEvent(override val aggregateId: LobbyId, val createdBy: PlayerId) :
    LobbyEvent

data class PlayerJoinedEvent(override val aggregateId: LobbyId, val playerId: PlayerId) :
    LobbyEvent

data class GameStartedEvent(override val aggregateId: LobbyId) : LobbyEvent

data class RoundStartedEvent(
    override val aggregateId: LobbyId,
    val cardsDealt: Map<PlayerId, List<Card>>,
) : LobbyEvent

data class BidPlacedEvent(override val aggregateId: LobbyId, val playerId: PlayerId, val bid: Bid) :
    LobbyEvent

data class CardPlayedEvent(
    override val aggregateId: LobbyId,
    val playerId: PlayerId,
    val card: Card,
) : LobbyEvent
