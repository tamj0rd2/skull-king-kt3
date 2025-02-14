package com.tamj0rd2.skullking.serialization.json

import com.tamj0rd2.skullking.domain.game.BidPlacedEvent
import com.tamj0rd2.skullking.domain.game.Card
import com.tamj0rd2.skullking.domain.game.CardPlayedEvent
import com.tamj0rd2.skullking.domain.game.GameStartedEvent
import com.tamj0rd2.skullking.domain.game.LobbyCreatedEvent
import com.tamj0rd2.skullking.domain.game.LobbyEvent
import com.tamj0rd2.skullking.domain.game.PlayerJoinedEvent
import com.tamj0rd2.skullking.domain.game.RoundStartedEvent
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JList
import com.ubertob.kondor.json.JMap
import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.ObjectNodeConverter
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.num
import com.ubertob.kondor.json.obj
import com.ubertob.kondor.json.str

object JLobbyEvent : JSealed<LobbyEvent>() {
    private val config =
        listOf(
            Triple(LobbyCreatedEvent::class, "game-created", JLobbyCreated),
            Triple(PlayerJoinedEvent::class, "player-joined", JPlayerJoined),
            Triple(GameStartedEvent::class, "game-started", JGameStarted),
            Triple(RoundStartedEvent::class, "round-started-event", JCardDealt),
            Triple(BidPlacedEvent::class, "bid-placed", JBidPlaced),
            Triple(CardPlayedEvent::class, "card-played", JCardPlayed),
        )

    override val subConverters: Map<String, ObjectNodeConverter<out LobbyEvent>>
        get() = config.associate { (_, eventType, converter) -> eventType to converter }

    override fun extractTypeName(obj: LobbyEvent): String {
        val configForThisObj = config.firstOrNull { (clazz, _, _) -> clazz == obj::class }
        checkNotNull(configForThisObj) { "Configure parsing for event type ${obj::class.java.simpleName}" }
        return configForThisObj.second
    }
}

private object JLobbyCreated : JAny<LobbyCreatedEvent>() {
    private val lobbyId by str(JLobbyId, LobbyCreatedEvent::aggregateId)
    private val createdBy by str(JPlayerId, LobbyCreatedEvent::createdBy)

    override fun JsonNodeObject.deserializeOrThrow() =
        LobbyCreatedEvent(
            aggregateId = +lobbyId,
            createdBy = +createdBy,
        )
}

private object JPlayerJoined : JAny<PlayerJoinedEvent>() {
    private val playerId by str(JPlayerId, PlayerJoinedEvent::playerId)
    private val lobbyId by str(JLobbyId, PlayerJoinedEvent::aggregateId)

    override fun JsonNodeObject.deserializeOrThrow() =
        PlayerJoinedEvent(
            playerId = +playerId,
            aggregateId = +lobbyId,
        )
}

private object JGameStarted : JAny<GameStartedEvent>() {
    private val lobbyId by str(JLobbyId, GameStartedEvent::aggregateId)

    override fun JsonNodeObject.deserializeOrThrow() =
        GameStartedEvent(
            aggregateId = +lobbyId,
        )
}

private object JCardDealt : JAny<RoundStartedEvent>() {
    private val lobbyId by str(JLobbyId, RoundStartedEvent::aggregateId)
    private val cardsDealt by obj(JMap(JPlayerId, JList(JSingleton(Card))), RoundStartedEvent::cardsDealt)

    override fun JsonNodeObject.deserializeOrThrow() =
        RoundStartedEvent(
            aggregateId = +lobbyId,
            cardsDealt = +cardsDealt,
        )
}

private object JBidPlaced : JAny<BidPlacedEvent>() {
    private val lobbyId by str(JLobbyId, BidPlacedEvent::aggregateId)
    private val playerId by str(JPlayerId, BidPlacedEvent::playerId)
    private val bid by num(JBid, BidPlacedEvent::bid)

    override fun JsonNodeObject.deserializeOrThrow() =
        BidPlacedEvent(
            aggregateId = +lobbyId,
            playerId = +playerId,
            bid = +bid,
        )
}

private object JCardPlayed : JAny<CardPlayedEvent>() {
    private val lobbyId by str(JLobbyId, CardPlayedEvent::aggregateId)
    private val playerId by str(JPlayerId, CardPlayedEvent::playerId)

    override fun JsonNodeObject.deserializeOrThrow() =
        CardPlayedEvent(
            aggregateId = +lobbyId,
            playerId = +playerId,
            card = Card,
        )
}
