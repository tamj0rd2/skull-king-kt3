package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCaseContract
import com.tamj0rd2.skullking.application.port.input.testsupport.TestScenario
import org.junit.platform.commons.annotation.Testable

@Testable
class PlaceABidWebAdapterTest : PlaceABidUseCaseContract {
    override val scenario: TestScenario = WebTestScenario()
}
