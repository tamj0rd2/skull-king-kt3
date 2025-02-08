package com.tamj0rd2.skullking.adapter.postgres

import com.tamj0rd2.skullking.application.port.output.EventStoreContract
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.application.port.output.LobbyRepositoryContract
import org.junit.jupiter.api.Disabled
import org.junit.platform.commons.annotation.Testable

@Testable
class EventStorePostgresAdapterTest :
    EventStoreContract,
    LobbyRepositoryContract {
    private companion object {
        val eventStore = EventStorePostgresAdapter.forLobbyEvents()
    }

    override val eventStore = EventStorePostgresAdapterTest.eventStore
    override val lobbyRepository = LobbyRepository(eventStore)

    override val propertyTestIterations = 10

    @Disabled
    override fun `can subscribe to receive game events`() {
        super.`can subscribe to receive game events`()
    }
}
