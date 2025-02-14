package com.tamj0rd2.skullking.adapter.postgres

import com.tamj0rd2.skullking.application.port.output.EventStoreContract
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.application.port.output.LobbyRepositoryContract
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD
import org.junit.platform.commons.annotation.Testable

// TODO: these tests fail when run in parallel
@Execution(SAME_THREAD)
@Testable
class EventStorePostgresAdapterTest :
    EventStoreContract,
    LobbyRepositoryContract {
    private companion object {
        @AutoClose
        val eventStore = EventStorePostgresAdapter.forLobbyEvents()
    }

    override val eventStore = EventStorePostgresAdapterTest.eventStore
    override val lobbyRepository = LobbyRepository(eventStore)

    override val propertyTestIterations = 10
}
