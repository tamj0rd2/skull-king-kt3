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

// TODO: at some point, use a connection pool instead.
class EventStorePostgresAdapter<ID : AggregateId, E : Event<ID>>(
    private val connectionString: String,
    private val eventConverter: ObjectNodeConverter<E>,
) : EventStore<ID, E> {
    // TODO: the transaction should be passed by the caller. look up suggested approaches for this.
    init {
        // TODO: table name should not be hardcoded!!
        // TODO: eventually, use migrations instead.
        startTransaction { connection ->
            connection
                .prepareStatement(
                    // language=PostgreSQL
                    """
                    |create table if not exists lobby_events (
                    |    lobbyId uuid not null,
                    |    payload jsonb,
                    |    revision integer
                    |)
                    """.trimMargin(),
                ).execute()
        }
    }

    override fun append(
        entityId: ID,
        expectedVersion: Version,
        events: Collection<E>,
    ) {
        // TODO: table name should not be hardcoded!!
        startTransaction { connection ->
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
