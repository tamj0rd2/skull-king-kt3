package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.ApplicationDomainDriver
import com.tamj0rd2.skullking.ApplicationDriver
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCaseContract
import com.tamj0rd2.skullking.port.output.GameEventsInMemoryAdapter

class ViewPlayerGameStateServiceTest : ViewPlayerGameStateUseCaseContract() {
    private val driver =
        ApplicationDomainDriver.create(
            gameRepository = GameEventsInMemoryAdapter(),
        )

    override fun newDriver(): ApplicationDriver = driver
}
