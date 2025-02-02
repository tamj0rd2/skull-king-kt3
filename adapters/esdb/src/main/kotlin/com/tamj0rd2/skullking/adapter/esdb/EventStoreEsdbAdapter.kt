package com.tamj0rd2.skullking.adapter.esdb

import com.eventstore.dbclient.AppendToStreamOptions
import com.eventstore.dbclient.CreatePersistentSubscriptionToStreamOptions
import com.eventstore.dbclient.EventData
import com.eventstore.dbclient.EventDataBuilder
import com.eventstore.dbclient.EventStoreDBClient
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
import com.tamj0rd2.skullking.domain.game.Version
import com.ubertob.kondor.json.JSealed
import java.util.concurrent.ExecutionException

class EventStoreEsdbAdapter<ID, Event : Any>(
    private val streamNameProvider: StreamNameProvider<ID>,
    private val converter: JSealed<Event>,
    // TODO: this stream name shouldn't be defaulted.
    private val subscriptionStreamName: String = "\$ce-lobby",
    private val subscriptionGroup: String = "spike-subscriptions",
    initialSubscribers: List<EventStoreSubscriber<Event>> = emptyList(),
) : EventStore<ID, Event> {
    private val subscribers = mutableListOf(*initialSubscribers.toTypedArray())

    data class StreamNameProvider<ID>(
        private val prefix: String,
        private val idToString: (ID) -> String,
    ) {
        fun streamNameFor(id: ID) = "$prefix-${idToString(id)}"
    }

    private val connectionString = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false")
    private val client = EventStoreDBClient.create(connectionString)
    private val subscriptionClient = EventStoreDBPersistentSubscriptionsClient.create(connectionString)

    init {
        startPersistentSubscription()
    }

    override fun read(entityId: ID): Collection<Event> = readEvents(entityId, ReadStreamOptions.get().forwards())

    override fun read(
        entityId: ID,
        upToAndIncludingVersion: Version,
    ): Collection<Event> = readEvents(entityId, ReadStreamOptions.get().forwards().maxCount(upToAndIncludingVersion.value.toLong()))

    override fun subscribe(subscriber: EventStoreSubscriber<Event>) {
        subscribers.add(subscriber)
    }

    // TODO: rename events to eventsToWrite
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
    ): List<Event> =
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
                        println("ESDB - subscription cancelled")
                        if (exception != null) throw exception
                    }

                    override fun onEvent(
                        subscription: PersistentSubscription,
                        retryCount: Int,
                        resolvedEvent: ResolvedEvent,
                    ) {
                        val event = converter.fromJson(resolvedEvent.dataAsString()).orThrow()
                        subscribers.forEach { it.receive(listOf(event)) }
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

    private fun ExpectedRevision.asVersion() = Version.of(this.toRawLong().toInt())

    private fun ResolvedEvent.dataAsString() = event.eventData.toString(Charsets.UTF_8)
}
