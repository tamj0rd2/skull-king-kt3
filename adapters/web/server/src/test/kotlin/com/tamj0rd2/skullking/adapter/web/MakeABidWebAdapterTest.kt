package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.MakeABidUseCaseContract
import com.tamj0rd2.skullking.application.port.input.testsupport.TestScenario
import org.junit.jupiter.api.Disabled
import org.junit.platform.commons.annotation.Testable

@Testable
class MakeABidWebAdapterTest : MakeABidUseCaseContract {
    override val scenario: TestScenario = WebTestScenario()

   @Disabled("TODO: make it work for web")
    override fun `bid values are revealed once everyone has made their bids`() {
        super.`bid values are revealed once everyone has made their bids`()
    }
}
