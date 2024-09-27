package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.ApplicationDomainDriver
import com.tamj0rd2.skullking.application.ApplicationDriver
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCaseContract
import com.tamj0rd2.skullking.application.port.output.GameRepositoryInMemoryAdapter

class CreateNewGameServiceTest : CreateNewGameUseCaseContract() {
    private val gameRepository = GameRepositoryInMemoryAdapter()

    override fun newDriver(): ApplicationDriver =
        ApplicationDomainDriver(
            gameRepository = gameRepository,
        )
}
