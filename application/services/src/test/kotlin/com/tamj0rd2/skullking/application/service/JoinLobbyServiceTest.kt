package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCaseContract
import org.junit.platform.commons.annotation.Testable

@Testable
class JoinLobbyServiceTest : JoinALobbyUseCaseContract {
    override val scenario = InMemoryTestScenario()
}
