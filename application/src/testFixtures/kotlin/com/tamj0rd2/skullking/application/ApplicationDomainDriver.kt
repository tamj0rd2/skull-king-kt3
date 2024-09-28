package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.port.input.InMemoryEventListeners
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameRepositoryInMemoryAdapter

fun ApplicationDomainDriver.Companion.usingTestDoublesByDefault(
    eventListeners: InMemoryEventListeners = InMemoryEventListeners(),
    gameRepository: GameRepository = GameRepositoryInMemoryAdapter(eventListeners),
): ApplicationDomainDriver =
    ApplicationDomainDriver(
        gameRepository = gameRepository,
    )
