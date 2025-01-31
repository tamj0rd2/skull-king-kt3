package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.adapter.inmemory.EventStoreInMemoryAdapter
import com.tamj0rd2.skullking.adapter.inmemory.LobbyNotifierInMemoryAdapter
import com.tamj0rd2.skullking.adapter.inmemory.PlayerIdStorageInMemoryAdapter
import com.tamj0rd2.skullking.application.port.output.LobbyRepository

fun SkullKingApplication.Companion.usingTestDoublesByDefault(): SkullKingApplication {
    val playerIdStorage = PlayerIdStorageInMemoryAdapter()

    return SkullKingApplication(
        lobbyRepository = LobbyRepository(EventStoreInMemoryAdapter()),
        lobbyNotifier = LobbyNotifierInMemoryAdapter(),
        findPlayerIdPort = playerIdStorage,
        savePlayerIdPort = playerIdStorage,
    )
}
