package com.tamj0rd2.skullking.adapter.esdb

import com.tamj0rd2.skullking.application.port.output.LobbyRepositoryContract
import io.kotest.property.PropertyTesting

class LobbyRepositoryEsdbAdapterTest : LobbyRepositoryContract() {
    companion object {
        // here so that I only need to deal with establishing a connection once
        val repo = LobbyRepositoryEsdbAdapter()
    }

    override val lobbyRepository: LobbyRepositoryEsdbAdapter = repo

    init {
        PropertyTesting.defaultIterationCount = 10
    }
}
