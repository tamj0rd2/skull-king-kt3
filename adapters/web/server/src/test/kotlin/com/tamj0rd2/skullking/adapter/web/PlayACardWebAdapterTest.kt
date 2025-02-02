package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.PlayACardUseCaseContract
import org.junit.jupiter.api.AutoClose
import org.junit.platform.commons.annotation.Testable

@Testable
class PlayACardWebAdapterTest : PlayACardUseCaseContract {
    override val propertyTestIterations = 10

    @AutoClose
    override val scenario = WebTestScenario()
}
