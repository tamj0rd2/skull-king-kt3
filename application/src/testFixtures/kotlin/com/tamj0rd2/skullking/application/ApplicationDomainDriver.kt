package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameRepositoryInMemoryAdapter
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifierInMemoryAdapter
import com.tamj0rd2.skullking.application.port.output.PlayerIdStorage
import com.tamj0rd2.skullking.application.port.output.PlayerIdStorageInMemoryAdapter

fun SkullKingApplication.Companion.usingTestDoublesByDefault(
    gameRepository: GameRepository = GameRepositoryInMemoryAdapter(),
    gameUpdateNotifier: GameUpdateNotifierInMemoryAdapter = GameUpdateNotifierInMemoryAdapter(),
    playerIdStorage: PlayerIdStorage = PlayerIdStorageInMemoryAdapter(),
): SkullKingApplication =
    SkullKingApplication(
        gameRepository = gameRepository,
        gameUpdateNotifier = gameUpdateNotifier,
        findPlayerIdPort = playerIdStorage,
        savePlayerIdPort = playerIdStorage,
    )
