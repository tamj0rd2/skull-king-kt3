package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.EndToEndTestContract
import com.tamj0rd2.skullking.application.ports.input.TestScenario
import org.junit.platform.commons.annotation.Testable

@Testable
class ApplicationEndToEndTest : EndToEndTestContract {
    override val scenario: TestScenario = ApplicationTestScenario()
}
