package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.StartGameUseCaseContract
import io.kotest.property.PropertyTesting

class StartGameWebAdapterTest : StartGameUseCaseContract() {
    init {
        PropertyTesting.defaultIterationCount = 10
    }

    override val scenario = WebTestScenario()
}
