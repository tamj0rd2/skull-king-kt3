package com.tamj0rd2.skullking.adapter.esdb

import com.eventstore.dbclient.AppendToStreamOptions
import com.eventstore.dbclient.EventDataBuilder
import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBConnectionString
import com.eventstore.dbclient.ExpectedRevision
import com.eventstore.dbclient.ReadStreamOptions
import com.eventstore.dbclient.ResolvedEvent
import com.eventstore.dbclient.StreamNotFoundException
import com.tamj0rd2.skullking.application.port.output.EventStore
import com.tamj0rd2.skullking.domain.game.Version
import com.ubertob.kondor.json.JSealed
import java.util.concurrent.ExecutionException

class EsdbEventStore<ID, Event : Any>(
    private val converter: JSealed<Event>,
    private val getStreamName: (ID) -> String,
) : EventStore<ID, Event> {
    private val client: EventStoreDBClient =
        EventStoreDBClient.create(
            EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false"),
        )

    override fun append(
        entityId: ID,
        expectedVersion: Version,
        events: Collection<Event>,
    ) {
        val eventData =
            events.map {
                EventDataBuilder
                    .json(
                        converter.extractTypeName(it),
                        converter.toJson(it).toByteArray(Charsets.UTF_8),
                    ).build()
            }

        client
            .appendToStream(
                getStreamName(entityId),
                AppendToStreamOptions.get().expectedRevision(expectedVersion.asExpectedRevision()),
                eventData.iterator(),
            ).get()
    }

    override fun read(entityId: ID): Collection<Event> =
        readEvents(getStreamName(entityId)).map { converter.fromJson(it.dataAsString()).orThrow() }

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

    // TODO: Version doesn't necessarily need to be part of the Game package.
    private fun Version.asExpectedRevision() =
        if (this == Version.NONE) {
            ExpectedRevision.noStream()
        } else {
            ExpectedRevision.expectedRevision(this.value.toLong() - 1)
        }

    private fun ResolvedEvent.dataAsString() = event.eventData.toString(Charsets.UTF_8)
}
