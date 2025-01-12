package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCaseContract
import org.junit.platform.commons.annotation.Testable

@Testable
class CreateNewGameServiceTest : CreateNewGameUseCaseContract {
    override val scenario = InMemoryTestScenario()
}
