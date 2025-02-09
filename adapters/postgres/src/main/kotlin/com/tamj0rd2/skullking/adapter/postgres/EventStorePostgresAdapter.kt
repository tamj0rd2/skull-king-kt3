package com.tamj0rd2.skullking.adapter.postgres

import com.tamj0rd2.skullking.application.port.output.EventStore
import com.tamj0rd2.skullking.application.port.output.EventStoreSubscriber
import com.tamj0rd2.skullking.domain.AggregateId
import com.tamj0rd2.skullking.domain.Event
import com.tamj0rd2.skullking.domain.game.Version
import com.tamj0rd2.skullking.serialization.json.JLobbyEvent
import com.ubertob.kondor.json.ObjectNodeConverter
import java.sql.Connection
import java.sql.Connection.TRANSACTION_SERIALIZABLE
import java.sql.DriverManager
import java.sql.ResultSet

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
        startTransaction { connection ->
            try {
                updateRevisionTable(connection, entityId, expectedVersion, events)
            } catch (e: ConcurrentModificationException) {
                // FIXME: this could become quite non-performant at some point. I might want to think about removing
                //  this idempotency thing, although it's kinda cool.
                //  Or rather than removing it, an improvement could be to only ready events after the expected version.
                val savedEvents = read(entityId).drop(expectedVersion.value)
                if (savedEvents == events) return@startTransaction
                throw e
            }

            // TODO: table name should not be hardcoded!!
            connection
                .prepareStatement(
                    // language=PostgreSQL
                    """
                    |insert into lobby_events (lobbyid, revision, payload)
                    |VALUES (?, ?, CAST(? as jsonb))
                    """.trimMargin(),
                ).use { preparedStatement ->
                    events.forEachIndexed { index, event ->
                        preparedStatement.setObject(1, entityId.value)
                        preparedStatement.setInt(2, expectedVersion.plus(index + 1).value)
                        preparedStatement.setString(3, eventConverter.toJson(event))
                        preparedStatement.addBatch()
                    }

                    preparedStatement.executeBatch()
                }
        }
    }

    override fun read(entityId: ID): Collection<E> =
        startTransaction { connection ->
            connection
                .prepareStatement(
                    // language=PostgreSQL
                    """
                    |select payload from lobby_events
                    |where lobbyid = ?
                    |order by revision
                    """.trimMargin(),
                ).use { preparedStatement ->
                    preparedStatement.setObject(1, entityId.value)
                    preparedStatement.executeQuery().use { resultSet ->
                        resultSet
                            .asSequence()
                            .map { resultSet.getString("payload") }
                            .map { eventConverter.fromJson(it).orThrow() }
                            .toList()
                    }
                }
        }

    override fun subscribe(subscriber: EventStoreSubscriber<ID, E>) {
        TODO("Not yet implemented")
    }

    private infix fun <T> startTransaction(block: (Connection) -> T): T =
        DriverManager.getConnection(connectionString).use { connection: Connection ->
            connection.transactionIsolation = TRANSACTION_SERIALIZABLE
            connection.autoCommit = false

            try {
                block(connection).also { connection.commit() }
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }

    private fun updateRevisionTable(
        connection: Connection,
        entityId: ID,
        expectedVersion: Version,
        events: Collection<E>,
    ) {
        val affectedRowCount =
            connection
                .prepareStatement(
                    // language=PostgreSQL
                    """
                    |insert into lobby_revisions (lobbyid, latest_revision)
                    |values (?, ?)
                    |on conflict (lobbyid)
                    |do update set latest_revision = ?
                    |where lobby_revisions.latest_revision = ?
                    """.trimMargin(),
                ).use { preparedStatement ->
                    val updatedLatestRevision = expectedVersion.plus(events.size).value

                    preparedStatement.setObject(1, entityId.value)
                    preparedStatement.setInt(2, updatedLatestRevision)
                    preparedStatement.setInt(3, updatedLatestRevision)
                    preparedStatement.setInt(4, expectedVersion.value)
                    preparedStatement.executeUpdate()
                }

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

        private fun ResultSet.asSequence() = generateSequence { if (next()) this else null }
    }
}
