package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCaseContract
import io.kotest.property.PropertyTesting

class JoinGameWebAdapterTest : JoinAGameUseCaseContract {
    init {
        PropertyTesting.defaultIterationCount = 10
    }

    override val scenario = WebTestScenario()
}
