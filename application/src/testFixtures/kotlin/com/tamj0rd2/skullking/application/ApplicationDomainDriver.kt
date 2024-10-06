package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameRepositoryInMemoryAdapter
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifierInMemoryAdapter

fun SkullKingApplication.Companion.usingTestDoublesByDefault(
    gameRepository: GameRepository = GameRepositoryInMemoryAdapter(),
    gameUpdateNotifierInMemory: GameUpdateNotifierInMemoryAdapter = GameUpdateNotifierInMemoryAdapter(),
): SkullKingApplication =
    SkullKingApplication(
        gameRepository = gameRepository,
        gameUpdateNotifier = gameUpdateNotifierInMemory,
    )
