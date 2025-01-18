package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.PlayACardUseCaseContract
import io.kotest.property.PropertyTesting
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.api.Disabled
import org.junit.platform.commons.annotation.Testable

@Testable
class PlayACardWebAdapterTest : PlayACardUseCaseContract {
    init {
        PropertyTesting.defaultIterationCount = 10
    }

    @AutoClose
    override val scenario = WebTestScenario()

    @Disabled
    override fun `when all players have played their cards, the winner of the trick is determined`() {
        super.`when all players have played their cards, the winner of the trick is determined`()
    }
}
