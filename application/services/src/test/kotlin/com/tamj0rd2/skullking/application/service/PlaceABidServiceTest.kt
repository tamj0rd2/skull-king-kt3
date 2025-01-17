package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCaseContract
import org.junit.platform.commons.annotation.Testable

@Testable
class PlaceABidServiceTest : PlaceABidUseCaseContract {
    override val scenario = InMemoryTestScenario()
}
