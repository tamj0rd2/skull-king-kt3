package com.tamj0rd2.skullking.adapter.esdb

import com.eventstore.dbclient.AppendToStreamOptions
import com.eventstore.dbclient.EventDataBuilder
import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBConnectionString
import com.eventstore.dbclient.ExpectedRevision
import com.eventstore.dbclient.ReadStreamOptions
import com.eventstore.dbclient.ResolvedEvent
import com.eventstore.dbclient.StreamNotFoundException
import com.tamj0rd2.skullking.application.port.output.LobbyDoesNotExist
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.domain.game.BidPlacedEvent
import com.tamj0rd2.skullking.domain.game.CardDealtEvent
import com.tamj0rd2.skullking.domain.game.GameStartedEvent
import com.tamj0rd2.skullking.domain.game.Lobby
import com.tamj0rd2.skullking.domain.game.LobbyCreatedEvent
import com.tamj0rd2.skullking.domain.game.LobbyEvent
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerJoinedEvent
import com.tamj0rd2.skullking.domain.game.Version
import com.tamj0rd2.skullking.serialization.json.JBid
import com.tamj0rd2.skullking.serialization.json.JLobbyId
import com.tamj0rd2.skullking.serialization.json.JPlayerId
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.ObjectNodeConverter
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.num
import com.ubertob.kondor.json.str
import java.util.concurrent.ExecutionException

class LobbyRepositoryEsdbAdapter : LobbyRepository {
    private val client: EventStoreDBClient =
        EventStoreDBClient.create(
            EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false"),
        )

    override fun load(lobbyId: LobbyId): Lobby {
        val events =
            readEvents(lobbyId.asStreamName())
                .asSequence()
                .map { it.event.eventData }
                .map { it.toString(Charsets.UTF_8) }
                .map { JLobbyEvent.fromJson(it).orThrow() }
                .filter { it.lobbyId == lobbyId }
                .toList()
                .ifEmpty { throw LobbyDoesNotExist() }

        return Lobby.from(events)
    }

    override fun save(lobby: Lobby) {
        val eventData =
            lobby.newEventsSinceLobbyWasLoaded.map {
                EventDataBuilder
                    .json(
                        JLobbyEvent.extractTypeName(it),
                        JLobbyEvent.toJson(it).toByteArray(Charsets.UTF_8),
                    ).build()
            }

        client
            .appendToStream(
                lobby.id.asStreamName(),
                AppendToStreamOptions.get().expectedRevision(lobby.expectedRevision),
                eventData.iterator(),
            ).get()
    }

    private fun readEvents(streamName: String): List<ResolvedEvent> =
        try {
            client.readStream(streamName, ReadStreamOptions.get().forwards()).get().events
        } catch (e: ExecutionException) {
            if (e.cause is StreamNotFoundException) {
                emptyList()
            } else {
                throw e
            }
        }

    companion object {
        private const val STREAM_PREFIX = "lobby-events"

        private fun LobbyId.asStreamName() = "$STREAM_PREFIX-${LobbyId.show(this)}"

        private val Lobby.expectedRevision
            get() =
                if (loadedAtVersion == Version.NONE) {
                    ExpectedRevision.noStream()
                } else {
                    ExpectedRevision.expectedRevision(loadedAtVersion.value.toLong() - 1)
                }

        private object JLobbyEvent : JSealed<LobbyEvent>() {
            private val config =
                listOf(
                    Triple(LobbyCreatedEvent::class, "game-created", JLobbyCreated),
                    Triple(PlayerJoinedEvent::class, "player-joined", JPlayerJoined),
                    Triple(GameStartedEvent::class, "game-started", JGameStarted),
                    Triple(CardDealtEvent::class, "card-dealt-event", JCardDealt),
                    Triple(BidPlacedEvent::class, "bid-placed", JBidPlaced),
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
            private val lobbyId by str(JLobbyId, LobbyCreatedEvent::lobbyId)
            private val createdBy by str(JPlayerId, LobbyCreatedEvent::createdBy)

            override fun JsonNodeObject.deserializeOrThrow() =
                LobbyCreatedEvent(
                    lobbyId = +lobbyId,
                    createdBy = +createdBy,
                )
        }

        private object JPlayerJoined : JAny<PlayerJoinedEvent>() {
            private val playerId by str(JPlayerId, PlayerJoinedEvent::playerId)
            private val lobbyId by str(JLobbyId, PlayerJoinedEvent::lobbyId)

            override fun JsonNodeObject.deserializeOrThrow() =
                PlayerJoinedEvent(
                    playerId = +playerId,
                    lobbyId = +lobbyId,
                )
        }

        private object JGameStarted : JAny<GameStartedEvent>() {
            private val lobbyId by str(JLobbyId, GameStartedEvent::lobbyId)

            override fun JsonNodeObject.deserializeOrThrow() =
                GameStartedEvent(
                    lobbyId = +lobbyId,
                )
        }

        private object JCardDealt : JAny<CardDealtEvent>() {
            private val lobbyId by str(JLobbyId, CardDealtEvent::lobbyId)

            override fun JsonNodeObject.deserializeOrThrow() =
                CardDealtEvent(
                    lobbyId = +lobbyId,
                )
        }

        private object JBidPlaced : JAny<BidPlacedEvent>() {
            private val lobbyId by str(JLobbyId, BidPlacedEvent::lobbyId)
            private val playerId by str(JPlayerId, BidPlacedEvent::playerId)
            private val bid by num(JBid, BidPlacedEvent::bid)

            override fun JsonNodeObject.deserializeOrThrow() =
                BidPlacedEvent(
                    lobbyId = +lobbyId,
                    playerId = +playerId,
                    bid = +bid,
                )
        }
    }
}
