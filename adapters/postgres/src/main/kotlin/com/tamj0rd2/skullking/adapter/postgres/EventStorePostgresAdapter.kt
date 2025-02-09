package com.tamj0rd2.skullking.adapter.postgres

import com.tamj0rd2.skullking.adapter.postgres.tables.records.EventsRecord
import com.tamj0rd2.skullking.adapter.postgres.tables.references.AGGREGATES
import com.tamj0rd2.skullking.adapter.postgres.tables.references.EVENTS
import com.tamj0rd2.skullking.application.port.output.EventStore
import com.tamj0rd2.skullking.application.port.output.EventStoreSubscriber
import com.tamj0rd2.skullking.domain.AggregateId
import com.tamj0rd2.skullking.domain.Event
import com.tamj0rd2.skullking.domain.game.Version
import com.tamj0rd2.skullking.serialization.json.JLobbyEvent
import com.ubertob.kondor.json.ObjectNodeConverter
import org.jooq.DSLContext
import org.jooq.JSONB.jsonb
import org.jooq.SQLDialect.POSTGRES
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.Connection.TRANSACTION_SERIALIZABLE
import java.sql.DriverManager

// NOTE: this is a treasure trove - https://github.com/eugene-khyst/postgresql-event-sourcing/blob/main/README.md
// TODO: at some point, use a connection pool instead.
class EventStorePostgresAdapter<ID : AggregateId, E : Event<ID>>(
    private val connectionString: String,
    private val eventConverter: ObjectNodeConverter<E>,
) : EventStore<ID, E> {
    override fun append(
        entityId: ID,
        expectedVersion: Version,
        events: Collection<E>,
    ) {
        // TODO: the transaction should be passed by the caller. look up suggested approaches for this.
        startTransaction {
            try {
                updateAggregatesTable(entityId, expectedVersion, events)
            } catch (e: ConcurrentModificationException) {
                // FIXME: this could become quite non-performant at some point. I might want to think about removing
                //  this idempotency thing, although it's kinda cool.
                //  Or rather than removing it, an improvement could be to only ready events after the expected version.
                val savedEvents = read(entityId).drop(expectedVersion.value)
                if (savedEvents == events) return@startTransaction
                throw e
            }

            val recordsToInsert =
                events.mapIndexed { index, event ->
                    EventsRecord(
                        id = entityId.value,
                        payload = jsonb(eventConverter.toJson(event)),
                        revision = expectedVersion.plus(index + 1).value,
                    )
                }

            batchInsert(recordsToInsert).execute()
        }
    }

    override fun read(entityId: ID): Collection<E> =
        startTransaction {
            selectFrom(EVENTS)
                .where(EVENTS.ID.eq(entityId.value))
                .orderBy(EVENTS.REVISION)
                .fetch()
                .map { eventConverter.fromJson(it.payload!!.data()).orThrow() }
        }

    override fun subscribe(subscriber: EventStoreSubscriber<ID, E>) {
        TODO("Not yet implemented")
    }

    private infix fun <T> startTransaction(block: DSLContext.() -> T): T =
        DriverManager.getConnection(connectionString).use { connection ->
            connection.transactionIsolation = TRANSACTION_SERIALIZABLE
            connection.autoCommit = false

            try {
                connection.dsl(block).also { connection.commit() }
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }

    private fun DSLContext.updateAggregatesTable(
        entityId: ID,
        expectedVersion: Version,
        events: Collection<E>,
    ) {
        val latestRevisionToSet = expectedVersion.plus(events.size).value

        val affectedRowCount =
            insertInto(AGGREGATES)
                .columns(AGGREGATES.ID, AGGREGATES.LATEST_REVISION)
                .values(entityId.value, latestRevisionToSet)
                .onConflict(AGGREGATES.ID)
                .doUpdate()
                .set(AGGREGATES.LATEST_REVISION, latestRevisionToSet)
                .where(AGGREGATES.LATEST_REVISION.eq(expectedVersion.value))
                .execute()

        if (affectedRowCount != 1) {
            throw ConcurrentModificationException(
                "Expected most recent entity version to be $expectedVersion but it wasn't",
            )
        }
    }

    companion object {
        fun forLobbyEvents() =
            EventStorePostgresAdapter(
                // TODO: don't hardcode passwords :D
                connectionString = "jdbc:postgresql://localhost:5432/skullking?user=skullking&password=password",
                eventConverter = JLobbyEvent,
            )

        private fun <T> Connection.dsl(block: DSLContext.() -> T) = DSL.using(this, POSTGRES).run(block)
    }
}
