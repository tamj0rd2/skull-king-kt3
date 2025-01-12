package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCaseContract
import org.junit.platform.commons.annotation.Testable

@Testable
class JoinGameServiceTest : JoinAGameUseCaseContract {
    override val scenario = InMemoryTestScenario()
}
