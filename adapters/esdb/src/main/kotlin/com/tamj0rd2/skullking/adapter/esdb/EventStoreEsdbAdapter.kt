package com.tamj0rd2.skullking.adapter.esdb

import com.eventstore.dbclient.AppendToStreamOptions
import com.eventstore.dbclient.CreatePersistentSubscriptionToStreamOptions
import com.eventstore.dbclient.EventData
import com.eventstore.dbclient.EventDataBuilder
import com.eventstore.dbclient.EventStoreDBClient
import com.eventstore.dbclient.EventStoreDBClientSettings
import com.eventstore.dbclient.EventStoreDBConnectionString
import com.eventstore.dbclient.EventStoreDBPersistentSubscriptionsClient
import com.eventstore.dbclient.ExpectedRevision
import com.eventstore.dbclient.PersistentSubscription
import com.eventstore.dbclient.PersistentSubscriptionListener
import com.eventstore.dbclient.ReadStreamOptions
import com.eventstore.dbclient.ResolvedEvent
import com.eventstore.dbclient.StreamNotFoundException
import com.eventstore.dbclient.SubscribePersistentSubscriptionOptions
import com.eventstore.dbclient.WrongExpectedVersionException
import com.tamj0rd2.skullking.application.port.output.EventStore
import com.tamj0rd2.skullking.application.port.output.EventStoreSubscriber
import com.tamj0rd2.skullking.domain.EntityId
import com.tamj0rd2.skullking.domain.Event
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.Version
import com.tamj0rd2.skullking.serialization.json.JLobbyEvent
import com.ubertob.kondor.json.JSealed
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutionException

class EventStoreEsdbAdapter<ID : EntityId, E : Event<ID>>(
    private val streamNameProvider: StreamNameProvider<ID>,
    private val converter: JSealed<E>,
    private val subscriptionStreamName: String,
    private val subscriptionGroup: String = "subscriptions",
    clientSettings: EventStoreDBClientSettings = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false"),
) : EventStore<ID, E> {
    data class StreamNameProvider<ID>(
        private val prefix: String,
        private val idToString: (ID) -> String,
    ) {
        fun streamNameFor(id: ID) = "$prefix-${idToString(id)}"
    }

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val subscribers = mutableListOf<EventStoreSubscriber<ID, E>>()
    private val client = EventStoreDBClient.create(clientSettings)
    private val subscriptionClient = EventStoreDBPersistentSubscriptionsClient.create(clientSettings)

    init {
        startPersistentSubscription()
    }

    override fun read(entityId: ID): Collection<E> = readEvents(entityId, ReadStreamOptions.get().forwards())

    override fun read(
        entityId: ID,
        upToAndIncludingVersion: Version,
    ): Collection<E> = readEvents(entityId, ReadStreamOptions.get().forwards().maxCount(upToAndIncludingVersion.value.toLong()))

    override fun subscribe(subscriber: EventStoreSubscriber<ID, E>) {
        subscribers.add(subscriber)
    }

    override fun append(
        entityId: ID,
        expectedVersion: Version,
        events: Collection<E>,
    ) {
        val eventData =
            events.map {
                EventDataBuilder
                    .json(
                        converter.extractTypeName(it),
                        converter.toJson(it).toByteArray(Charsets.UTF_8),
                    ).build()
            }

        try {
            writeEvents(entityId, expectedVersion, eventData)
        } catch (e: ExecutionException) {
            if (e.cause !is WrongExpectedVersionException) throw e

            val eventsWrittenInTheMeantime = read(entityId).drop(expectedVersion.value)

            // attempting to write exactly the same events that have already been written. Idempotence!
            if (eventsWrittenInTheMeantime == events) return

            throw EventStore.concurrentModificationException(
                expectedVersion = expectedVersion,
                actualVersion = (e.cause as WrongExpectedVersionException).actualVersion.asVersion(),
            )
        }
    }

    private fun writeEvents(
        entityId: ID,
        expectedVersion: Version,
        eventData: List<EventData>,
    ) {
        client
            .appendToStream(
                entityId.toStreamName(),
                AppendToStreamOptions.get().expectedRevision(expectedVersion.asExpectedRevision()),
                eventData.iterator(),
            ).get()
    }

    private fun readEvents(
        entityId: ID,
        readStreamOptions: ReadStreamOptions?,
    ): List<E> =
        try {
            client
                .readStream(entityId.toStreamName(), readStreamOptions)
                .get()
                .events
                .map { converter.fromJson(it.dataAsString()).orThrow() }
        } catch (e: ExecutionException) {
            if (e.cause is StreamNotFoundException) {
                emptyList()
            } else {
                throw e
            }
        }

    private fun startPersistentSubscription() {
        try {
            subscriptionClient
                .createToStream(
                    subscriptionStreamName,
                    subscriptionGroup,
                    CreatePersistentSubscriptionToStreamOptions.get().resolveLinkTos(),
                ).get()
        } catch (e: ExecutionException) {
            // the next call will tell me whether something failed.
        }

        subscriptionClient
            .subscribeToStream(
                // TODO: the accumulation stream needs to be configurable.
                subscriptionStreamName,
                subscriptionGroup,
                SubscribePersistentSubscriptionOptions.get(),
                object : PersistentSubscriptionListener() {
                    override fun onCancelled(
                        subscription: PersistentSubscription?,
                        exception: Throwable?,
                    ) {
                        logger.info("ESDB - subscription cancelled")
                        if (exception != null) throw exception
                    }

                    override fun onEvent(
                        subscription: PersistentSubscription,
                        retryCount: Int,
                        resolvedEvent: ResolvedEvent,
                    ) {
                        // TODO: I shouldn't need to deserialize the entire payload. I could just use event.entityId, as long as I had
                        //  a way to deserialize just the ID.
                        val event = converter.fromJson(resolvedEvent.dataAsString()).orThrow()
                        val version = resolvedEvent.asVersion()
                        subscribers.forEach { it.onEventReceived(event.entityId, version) }
                        subscription.ack(resolvedEvent)
                    }
                },
            ).get()
    }

    private fun ID.toStreamName() = streamNameProvider.streamNameFor(this)

    // TODO: Version doesn't necessarily need to be part of the Game package.
    private fun Version.asExpectedRevision() =
        if (this == Version.NONE) {
            ExpectedRevision.noStream()
        } else {
            ExpectedRevision.expectedRevision(this.value.toLong() - 1)
        }

    private fun Long.toVersion() = Version.of(toInt() + 1)

    private fun ExpectedRevision.asVersion() = this.toRawLong().toVersion()

    private fun ResolvedEvent.asVersion() = event.revision.toVersion()

    private fun ResolvedEvent.dataAsString() = event.eventData.toString(Charsets.UTF_8)

    companion object {
        fun forLobbyEvents() =
            EventStoreEsdbAdapter(
                streamNameProvider =
                    StreamNameProvider(
                        prefix = "lobby-events",
                        idToString = LobbyId::show,
                    ),
                converter = JLobbyEvent,
                // TODO: make this part of StreamNameProvider, since it's possible to derive it.
                subscriptionStreamName = "\$ce-lobby",
            )
    }
}
