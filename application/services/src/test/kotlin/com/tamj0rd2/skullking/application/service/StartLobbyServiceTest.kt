package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.StartLobbyUseCaseContract
import org.junit.platform.commons.annotation.Testable

@Testable
class StartLobbyServiceTest : StartLobbyUseCaseContract {
    override val scenario = InMemoryTestScenario()
}
