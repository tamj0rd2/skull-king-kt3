package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.JoinGameUseCaseContract

class JoinGameServiceTest : JoinGameUseCaseContract() {
    override val scenario = InMemoryTestScenario()
}
