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

    @Disabled
    override fun `can load a lobby at a specific version`() {
        super.`can load a lobby at a specific version`()
    }

    @Disabled
    override fun `writes are idempotent if trying to make exactly the same change for exactly the same version`() {
        super.`writes are idempotent if trying to make exactly the same change for exactly the same version`()
    }

    @Disabled
    override fun `when optimistic concurrency fails, an error is thrown`() {
        super.`when optimistic concurrency fails, an error is thrown`()
    }

    @Disabled
    override fun `modifying, saving and loading a lobby multiple times results in the same state as just modifying the lobby in memory`() {
        super.`modifying, saving and loading a lobby multiple times results in the same state as just modifying the lobby in memory`()
    }
}
