package com.tamj0rd2.skullking.adapter

import com.eventstore.dbclient.AppendToStreamOptions
import com.eventstore.dbclient.EventDataBuilder
import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBConnectionString
import com.eventstore.dbclient.ExpectedRevision
import com.eventstore.dbclient.ReadStreamOptions
import com.eventstore.dbclient.ResolvedEvent
import com.eventstore.dbclient.StreamNotFoundException
import com.tamj0rd2.skullking.application.port.output.GameDoesNotExist
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.domain.model.game.CardDealtEvent
import com.tamj0rd2.skullking.domain.model.game.Game
import com.tamj0rd2.skullking.domain.model.game.GameCreatedEvent
import com.tamj0rd2.skullking.domain.model.game.GameEvent
import com.tamj0rd2.skullking.domain.model.game.GameId
import com.tamj0rd2.skullking.domain.model.game.GameStartedEvent
import com.tamj0rd2.skullking.domain.model.game.PlayerId
import com.tamj0rd2.skullking.domain.model.game.PlayerJoinedEvent
import com.tamj0rd2.skullking.domain.model.game.Version
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.JStringRepresentable
import com.ubertob.kondor.json.ObjectNodeConverter
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.str
import java.util.concurrent.ExecutionException
import kotlin.text.Charsets.UTF_8

class GameRepositoryEsdbAdapter : GameRepository {
    private val client: EventStoreDBClient =
        EventStoreDBClient.create(
            EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false"),
        )

    override fun load(gameId: GameId): Game {
        val events =
            readEvents(gameId.asStreamName())
                .asSequence()
                .map { it.event.eventData }
                .map { it.toString(UTF_8) }
                .map { JGameEvent.fromJson(it).orThrow() }
                .filter { it.gameId == gameId }
                .toList()
                .ifEmpty { throw GameDoesNotExist() }

        return Game.from(events)
    }

    override fun save(game: Game) {
        val eventData =
            game.newEvents.map {
                EventDataBuilder
                    .json(
                        JGameEvent.extractTypeName(it),
                        JGameEvent.toJson(it).toByteArray(UTF_8),
                    ).build()
            }

        client
            .appendToStream(
                game.id.asStreamName(),
                AppendToStreamOptions.get().expectedRevision(game.expectedRevision),
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
        private const val STREAM_PREFIX = "game-events"

        private fun GameId.asStreamName() = "$STREAM_PREFIX-${GameId.show(this)}"

        private val Game.expectedRevision
            get() =
                if (loadedVersion == Version.NONE) {
                    ExpectedRevision.noStream()
                } else {
                    ExpectedRevision.expectedRevision(loadedVersion.value.toLong())
                }

        private object JGameEvent : JSealed<GameEvent>() {
            private val config =
                listOf(
                    Triple(GameCreatedEvent::class, "game-created", JGameCreated),
                    Triple(PlayerJoinedEvent::class, "player-joined", JPlayerJoined),
                    Triple(GameStartedEvent::class, "game-started", JGameStarted),
                    Triple(CardDealtEvent::class, "card-dealt-event", JCardDealt),
                )

            override val subConverters: Map<String, ObjectNodeConverter<out GameEvent>>
                get() = config.associate { (_, eventType, converter) -> eventType to converter }

            override fun extractTypeName(obj: GameEvent): String {
                val configForThisObj = config.firstOrNull { (clazz, _, _) -> clazz == obj::class }
                checkNotNull(configForThisObj) { "Configure parsing for event type ${obj::class.java.simpleName}" }
                return configForThisObj.second
            }
        }

        private object JGameCreated : JAny<GameCreatedEvent>() {
            private val gameId by str(JGameId, GameCreatedEvent::gameId)

            override fun JsonNodeObject.deserializeOrThrow() =
                GameCreatedEvent(
                    gameId = +gameId,
                )
        }

        private object JPlayerJoined : JAny<PlayerJoinedEvent>() {
            private val playerId by str(JPlayerId, PlayerJoinedEvent::playerId)
            private val gameId by str(JGameId, PlayerJoinedEvent::gameId)

            override fun JsonNodeObject.deserializeOrThrow() =
                PlayerJoinedEvent(
                    playerId = +playerId,
                    gameId = +gameId,
                )
        }

        private object JGameStarted : JAny<GameStartedEvent>() {
            private val gameId by str(JGameId, GameStartedEvent::gameId)

            override fun JsonNodeObject.deserializeOrThrow() =
                GameStartedEvent(
                    gameId = +gameId,
                )
        }

        private object JCardDealt : JAny<CardDealtEvent>() {
            private val gameId by str(JGameId, CardDealtEvent::gameId)

            override fun JsonNodeObject.deserializeOrThrow() =
                CardDealtEvent(
                    gameId = +gameId,
                )
        }

        private object JPlayerId : JStringRepresentable<PlayerId>() {
            override val cons: (String) -> PlayerId = PlayerId.Companion::parse
            override val render: (PlayerId) -> String = PlayerId.Companion::show
        }

        private object JGameId : JStringRepresentable<GameId>() {
            override val cons: (String) -> GameId = GameId.Companion::parse
            override val render: (GameId) -> String = GameId.Companion::show
        }
    }
}
