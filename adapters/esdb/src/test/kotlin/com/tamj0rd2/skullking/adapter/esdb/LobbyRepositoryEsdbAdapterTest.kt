package com.tamj0rd2.skullking.adapter.esdb

import com.tamj0rd2.skullking.application.port.output.EventStoreContract
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.application.port.output.LobbyRepositoryContract
import org.junit.platform.commons.annotation.Testable

@Testable
class LobbyRepositoryEsdbAdapterTest :
    LobbyRepositoryContract,
    EventStoreContract {
    private companion object {
        val eventStore = EventStoreEsdbAdapter.forLobbyEvents()
    }

    override val eventStore = LobbyRepositoryEsdbAdapterTest.eventStore
    override val lobbyRepository = LobbyRepository(eventStore)

    override val propertyTestIterations = 10
}
