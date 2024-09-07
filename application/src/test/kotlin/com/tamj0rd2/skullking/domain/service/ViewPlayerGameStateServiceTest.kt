package com.tamj0rd2.skullking.domain.service

import com.tamj0rd2.skullking.ApplicationDomainDriver
import com.tamj0rd2.skullking.ApplicationDriver
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCaseContract
import com.tamj0rd2.skullking.port.output.GameEventsInMemoryAdapter

class ViewPlayerGameStateServiceTest : ViewPlayerGameStateUseCaseContract() {
    override val driver: ApplicationDriver = ApplicationDomainDriver(GameEventsInMemoryAdapter())
}
