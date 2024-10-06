package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.StartGameUseCaseContract
import org.junit.jupiter.api.Timeout

@Timeout(2)
class StartGameWebAdapterTest : StartGameUseCaseContract() {
    override val scenario = WebTestScenario()
}
