package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCaseContract
import com.tamj0rd2.skullking.application.port.input.testsupport.TestScenario
import io.kotest.property.PropertyTesting
import org.junit.jupiter.api.AutoClose
import org.junit.platform.commons.annotation.Testable

@Testable
class PlaceABidWebAdapterTest : PlaceABidUseCaseContract {
    init {
        PropertyTesting.defaultIterationCount = 10
    }

    @AutoClose
    override val scenario: TestScenario = WebTestScenario()
}
