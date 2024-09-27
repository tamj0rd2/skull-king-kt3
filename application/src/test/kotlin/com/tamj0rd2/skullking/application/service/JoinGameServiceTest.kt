package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.ApplicationDomainDriver
import com.tamj0rd2.skullking.application.ApplicationDriver
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCaseContract
import com.tamj0rd2.skullking.application.port.output.GameRepositoryInMemoryAdapter

class JoinGameServiceTest : JoinGameUseCaseContract() {
    private val driver =
        ApplicationDomainDriver(
            gameRepository = GameRepositoryInMemoryAdapter(),
        )

    override fun newDriver(): ApplicationDriver = driver
}
