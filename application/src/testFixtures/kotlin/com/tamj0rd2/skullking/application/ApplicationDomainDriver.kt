package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameRepositoryInMemoryAdapter

fun ApplicationDomainDriver.Companion.usingTestDoublesByDefault(
    gameRepository: GameRepository = GameRepositoryInMemoryAdapter(),
): ApplicationDomainDriver =
    ApplicationDomainDriver(
        gameRepository = gameRepository,
    )
