package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.application.port.output.LobbyRepositoryContract

class LobbyRepositoryInMemoryAdapterTests : LobbyRepositoryContract() {
    override val lobbyRepository: LobbyRepository = LobbyRepositoryInMemoryAdapter()
}
