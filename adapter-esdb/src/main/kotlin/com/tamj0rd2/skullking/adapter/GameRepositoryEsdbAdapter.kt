package com.tamj0rd2.skullking.adapter

import com.eventstore.dbclient.EventDataBuilder
import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBConnectionString
import com.eventstore.dbclient.ReadStreamOptions
import com.eventstore.dbclient.ResolvedEvent
import com.eventstore.dbclient.StreamNotFoundException
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.domain.model.GameCreated
import com.tamj0rd2.skullking.domain.model.GameEvent
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.PlayerJoined
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.JStringRepresentable
import com.ubertob.kondor.json.ObjectNodeConverter
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.str
import dev.forkhandles.result4k.recover
import dev.forkhandles.result4k.resultFromCatching
import kotlin.text.Charsets.UTF_8

class GameRepositoryEsdbAdapter(
    private val client: EventStoreDBClient =
        EventStoreDBClient.create(
            EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false"),
        ),
) : GameRepository {
    override fun load(gameId: GameId): Game = Game.from(findGameEvents(gameId))

    override fun save(game: Game) {
        saveGameEvents(game.changes)
    }

    override fun findGameEvents(gameId: GameId): List<GameEvent> {
        val events =
            resultFromCatching<StreamNotFoundException, List<ResolvedEvent>> {
                client.readStream("game-events", ReadStreamOptions.get().forwards()).get().events
            }.recover { emptyList() }

        return events
            .asSequence()
            .map { it.event.eventData }
            .map { it.toString(UTF_8) }
            .map { JGameEvent.fromJson(it).orThrow() }
            .filter { it.gameId == gameId }
            .toList()
    }

    override fun saveGameEvents(events: List<GameEvent>) {
        val eventData =
            events.map {
                EventDataBuilder
                    .json(
                        JGameEvent.extractTypeName(it),
                        JGameEvent.toJson(it).toByteArray(UTF_8),
                    ).build()
            }
        client.appendToStream("game-events", *eventData.toTypedArray()).get()
    }

    companion object {
        private object JGameEvent : JSealed<GameEvent>() {
            private val config =
                listOf(
                    Triple(GameCreated::class, "game-created", JGameCreated),
                    Triple(PlayerJoined::class, "player-joined", JPlayerJoined),
                )

            override val subConverters: Map<String, ObjectNodeConverter<out GameEvent>>
                get() = config.associate { (_, eventType, converter) -> eventType to converter }

            override fun extractTypeName(obj: GameEvent): String = config.single { (clazz, _, _) -> clazz == obj::class }.second
        }

        private object JGameCreated : JAny<GameCreated>() {
            private val gameId by str(JGameId, GameCreated::gameId)

            override fun JsonNodeObject.deserializeOrThrow() =
                GameCreated(
                    gameId = +gameId,
                )
        }

        private object JPlayerJoined : JAny<PlayerJoined>() {
            private val playerId by str(JPlayerId, PlayerJoined::playerId)
            private val gameId by str(JGameId, PlayerJoined::gameId)

            override fun JsonNodeObject.deserializeOrThrow() =
                PlayerJoined(
                    playerId = +playerId,
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
