package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameRepositoryInMemoryAdapter
import com.tamj0rd2.skullking.application.service.GameUpdateNotifierInMemoryAdapter

fun ApplicationDomainDriver.Companion.usingTestDoublesByDefault(
    gameRepository: GameRepository = GameRepositoryInMemoryAdapter(),
    gameUpdateNotifierInMemory: GameUpdateNotifierInMemoryAdapter = GameUpdateNotifierInMemoryAdapter(),
): ApplicationDomainDriver =
    ApplicationDomainDriver(
        gameRepository = gameRepository,
        gameUpdateNotifier = gameUpdateNotifierInMemory,
    )
