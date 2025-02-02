package com.tamj0rd2.skullking.adapter.esdb

import com.tamj0rd2.skullking.adapter.esdb.EventStoreEsdbAdapter.StreamNameProvider
import com.tamj0rd2.skullking.application.port.output.EventStoreContract
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.application.port.output.LobbyRepositoryContract
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.serialization.json.JLobbyEvent
import org.junit.platform.commons.annotation.Testable

@Testable
class LobbyRepositoryEsdbAdapterTest :
    LobbyRepositoryContract,
    EventStoreContract {
    private companion object {
        // here so that I only need to deal with establishing a connection once
        val eventStore =
            EventStoreEsdbAdapter(
                streamNameProvider =
                    StreamNameProvider(
                        prefix = "lobby-events",
                        idToString = LobbyId::show,
                    ),
                converter = JLobbyEvent,
            )
    }

    override val eventStore = LobbyRepositoryEsdbAdapterTest.eventStore
    override val lobbyRepository = LobbyRepository(eventStore)

    override val propertyTestIterations = 10
}
