package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.CreateNewGameGameUseCaseContract

class CreateNewGameServiceTest : CreateNewGameGameUseCaseContract() {
    override val scenario = InMemoryTestScenario()
}
