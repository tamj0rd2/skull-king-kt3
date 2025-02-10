package com.tamj0rd2.skullking.adapter.postgres

import com.tamj0rd2.skullking.adapter.postgres.tables.references.EVENTS
import com.tamj0rd2.skullking.application.port.output.EventStoreSubscriber
import com.tamj0rd2.skullking.domain.AggregateId
import com.tamj0rd2.skullking.domain.game.Version
import org.jooq.DSLContext
import org.jooq.SQLDialect.POSTGRES
import org.jooq.impl.DSL
import org.postgresql.jdbc.PgConnection
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicLong

internal class PostgresEventListener<ID : AggregateId>(
    private val connectionString: String,
    private val uuidToEntityId: (UUID) -> ID,
) : AutoCloseable {
    private data class AggregateChange<ID>(
        val id: Long,
        val aggregateId: ID,
        val revision: Version,
    )

    // there needs to be 1 persistent connection to listen to new events. but a pool can be used everywhere else.
    private val listenerConnection = getConnection()
    private val databaseListenerExecutor = Executors.newVirtualThreadPerTaskExecutor()
    private val scheduledExecutor = Executors.newScheduledThreadPool(100, Thread.ofVirtual().factory())

    // note: it'd be better for the checkpoint to live in the DB, but in memory is fine for now.
    private val subscribers = mutableMapOf<EventStoreSubscriber<ID, *>, AtomicLong>()

    fun start() {
        // notify/listen is way quicker
        databaseListenerExecutor.execute {
            listenerConnection.dsl { execute("listen channel_event_notify") }

            while (!listenerConnection.isClosed) {
                val notifications = (listenerConnection as PgConnection).getNotifications(0)
                if (notifications.isNotEmpty()) {
                    notifySubscribers(notifications.size)
                }
            }
        }

        // polling is still here as notify/listen doesn't guarantee delivery
        scheduledExecutor.scheduleAtFixedRate(
            { notifySubscribers(100) },
            0,
            5,
            SECONDS,
        )
    }

    fun newCatchupSubscription(subscriber: EventStoreSubscriber<ID, *>) {
        if (subscriber in subscribers) return
        if (subscribers.isNotEmpty()) TODO("multiple subscribers not supported yet")
        subscribers[subscriber] = AtomicLong(0)

        val totalEventCount = getConnection().dsl { fetchCount(EVENTS) }
        notifySubscribers(totalEventCount)
    }

    override fun close() {
        listenerConnection.close()
        databaseListenerExecutor.shutdown()
        scheduledExecutor.shutdown()
        scheduledExecutor.awaitTermination(2, SECONDS)
    }

    private fun notifySubscribers(limit: Int) {
        // this code could be non-performant or just not work correctly with multiple subscribers. That behaviour needs test driving.
        if (subscribers.size > 1) TODO("multiple subscribers not supported")

        subscribers.forEach { (subscriber, lastProcessedChange) ->
            val aggregateChangeList =
                getConnection().dsl {
                    select(EVENTS.ID, EVENTS.AGGREGATE_ID, EVENTS.REVISION)
                        .from(EVENTS)
                        .where(EVENTS.ID.greaterThan(lastProcessedChange.get()))
                        .limit(limit)
                        .fetch()
                        .toList()
                        .map {
                            AggregateChange(
                                id = it[EVENTS.ID]!!,
                                aggregateId = uuidToEntityId(it[EVENTS.AGGREGATE_ID]!!),
                                revision = Version.of(it[EVENTS.REVISION]!!),
                            )
                        }
                }

            aggregateChangeList.forEach { change ->
                // guarantees at-least-once delivery
                subscriber.onEventReceived(change.aggregateId, change.revision)
                lastProcessedChange.getAndIncrement()
            }
        }
    }

    private fun getConnection() = DriverManager.getConnection(connectionString)

    companion object {
        private fun <T> Connection.dsl(block: DSLContext.() -> T) = DSL.using(this, POSTGRES).run(block)
    }
}
