package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.StartGameUseCaseContract
import org.junit.platform.commons.annotation.Testable

@Testable
class StartGameServiceTest : StartGameUseCaseContract {
    override val scenario = InMemoryTestScenario()
}
