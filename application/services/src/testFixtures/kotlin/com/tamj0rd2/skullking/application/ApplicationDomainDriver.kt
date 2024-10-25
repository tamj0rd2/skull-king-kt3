package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.adapter.inmemory.GameRepositoryInMemoryAdapter
import com.tamj0rd2.skullking.adapter.inmemory.GameUpdateNotifierInMemoryAdapter
import com.tamj0rd2.skullking.adapter.inmemory.PlayerIdStorageInMemoryAdapter

fun SkullKingApplication.Companion.usingTestDoublesByDefault(): SkullKingApplication {
    val playerIdStorage = PlayerIdStorageInMemoryAdapter()

    return SkullKingApplication(
        gameRepository = GameRepositoryInMemoryAdapter(),
        gameUpdateNotifier = GameUpdateNotifierInMemoryAdapter(),
        findPlayerIdPort = playerIdStorage,
        savePlayerIdPort = playerIdStorage,
    )
}
