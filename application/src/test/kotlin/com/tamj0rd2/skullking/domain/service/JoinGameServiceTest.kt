package com.tamj0rd2.skullking.domain.service

import com.tamj0rd2.skullking.ApplicationDomainDriver
import com.tamj0rd2.skullking.ApplicationDriver
import com.tamj0rd2.skullking.port.input.JoinGameUseCaseContract
import com.tamj0rd2.skullking.port.output.GameEventsInMemoryAdapter

class JoinGameServiceTest : JoinGameUseCaseContract() {
    private val gameEventsPort = GameEventsInMemoryAdapter()

    override fun newDriver(): ApplicationDriver =
        ApplicationDomainDriver.create(
            gameEventsPort = gameEventsPort,
        )
}
