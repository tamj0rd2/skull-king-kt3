package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCaseContract

class JoinGameServiceTest : JoinAGameUseCaseContract {
    override val scenario = InMemoryTestScenario()
}
