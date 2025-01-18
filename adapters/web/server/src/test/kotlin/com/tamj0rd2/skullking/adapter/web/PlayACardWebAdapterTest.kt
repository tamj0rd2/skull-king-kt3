package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.PlayACardUseCaseContract
import org.junit.platform.commons.annotation.Testable

@Testable
class PlayACardWebAdapterTest : PlayACardUseCaseContract {
    override val scenario = WebTestScenario()
}
