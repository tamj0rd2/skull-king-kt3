package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.adapter.inmemory.LobbyNotifierInMemoryAdapter
import com.tamj0rd2.skullking.adapter.inmemory.LobbyRepositoryInMemoryAdapter
import com.tamj0rd2.skullking.adapter.inmemory.PlayerIdStorageInMemoryAdapter

fun SkullKingApplication.Companion.usingTestDoublesByDefault(): SkullKingApplication {
    val playerIdStorage = PlayerIdStorageInMemoryAdapter()

    return SkullKingApplication(
        lobbyRepository = LobbyRepositoryInMemoryAdapter(),
        lobbyNotifier = LobbyNotifierInMemoryAdapter(),
        findPlayerIdPort = playerIdStorage,
        savePlayerIdPort = playerIdStorage,
    )
}
