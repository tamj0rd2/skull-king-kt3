package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.PlayACardUseCaseContract
import org.junit.jupiter.api.Disabled
import org.junit.platform.commons.annotation.Testable

@Testable
@Disabled
class PlayACardServiceTest : PlayACardUseCaseContract {
    override val scenario = InMemoryTestScenario()
}
