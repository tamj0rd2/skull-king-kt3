package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCaseContract
import io.kotest.property.PropertyTesting

class CreateGameWebAdapterTest : CreateNewGameUseCaseContract() {
    init {
        PropertyTesting.defaultIterationCount = 10
    }

    override val scenario = WebTestScenario()
}
