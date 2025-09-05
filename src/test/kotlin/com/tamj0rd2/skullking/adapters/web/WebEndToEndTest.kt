package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.EndToEndTestContract
import com.tamj0rd2.skullking.application.ports.input.TestScenario
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Timeout(10, unit = TimeUnit.SECONDS)
class WebEndToEndTest : EndToEndTestContract {
    @AutoClose override val scenario: TestScenario = WebTestScenario()
}
