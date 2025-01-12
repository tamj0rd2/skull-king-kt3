package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCaseContract
import io.kotest.property.PropertyTesting
import org.junit.platform.commons.annotation.Testable

@Testable
class CreateGameWebAdapterTest : CreateNewGameUseCaseContract {
    init {
        PropertyTesting.defaultIterationCount = 10
    }

    override val scenario = WebTestScenario()
}
