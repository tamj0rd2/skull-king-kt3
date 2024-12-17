package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCaseContract

class CreateNewGameServiceTest : CreateNewGameUseCaseContract {
    override val scenario = InMemoryTestScenario()
}
