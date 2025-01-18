package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCaseContract
import io.kotest.property.PropertyTesting
import org.junit.jupiter.api.AutoClose
import org.junit.platform.commons.annotation.Testable

@Testable
class JoinGameWebAdapterTest : JoinAGameUseCaseContract {
    init {
        PropertyTesting.defaultIterationCount = 10
    }

    @AutoClose
    override val scenario = WebTestScenario()
}
