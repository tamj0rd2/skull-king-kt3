package com.tamj0rd2.skullking.adapter.postgres

import com.tamj0rd2.skullking.application.port.output.EventStoreContract
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.application.port.output.LobbyRepositoryContract
import org.junit.jupiter.api.AutoClose
import org.junit.platform.commons.annotation.Testable

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
