package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.output.EventStoreContract
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.application.port.output.LobbyRepositoryContract
import com.tamj0rd2.skullking.domain.game.LobbyEvent
import com.tamj0rd2.skullking.domain.game.LobbyId
import org.junit.platform.commons.annotation.Testable

@Testable
class LobbyRepositoryInMemoryAdapterTests : LobbyRepositoryContract, EventStoreContract {
    override val eventStore = EventStoreInMemoryAdapter<LobbyId, LobbyEvent>()
    override val lobbyRepository: LobbyRepository = LobbyRepository(eventStore)
}
