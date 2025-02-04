package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.StartGameUseCaseContract
import org.junit.jupiter.api.AutoClose
import org.junit.platform.commons.annotation.Testable

@Testable
class StartGameWebAdapterTest : StartGameUseCaseContract {
    override val propertyTestIterations = 10

    @AutoClose
    override val scenario = WebTestScenario()
}
