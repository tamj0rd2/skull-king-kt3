package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.adapter.GameUpdateNotifierInMemoryAdapter
import com.tamj0rd2.skullking.adapter.PlayerIdStorageInMemoryAdapter
import com.tamj0rd2.skullking.application.port.output.GameRepositoryInMemoryAdapter

fun SkullKingApplication.Companion.usingTestDoublesByDefault(): SkullKingApplication {
    val playerIdStorage = PlayerIdStorageInMemoryAdapter()

    return SkullKingApplication(
        gameRepository = GameRepositoryInMemoryAdapter(),
        gameUpdateNotifier = GameUpdateNotifierInMemoryAdapter(),
        findPlayerIdPort = playerIdStorage,
        savePlayerIdPort = playerIdStorage,
    )
}
