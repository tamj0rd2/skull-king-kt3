package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.MakeABidUseCaseContract
import org.junit.platform.commons.annotation.Testable

@Testable
class MakeABidServiceTest : MakeABidUseCaseContract {
    override val scenario = InMemoryTestScenario()
}