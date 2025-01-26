package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCaseContract
import org.junit.platform.commons.annotation.Testable

@Testable
class CreateNewLobbyServiceTest : CreateNewLobbyUseCaseContract {
    override val scenario = InMemoryTestScenario()
}
