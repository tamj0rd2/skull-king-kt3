package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCaseContract

class CreateGameWebAdapterTest : CreateNewGameUseCaseContract() {
    override val scenario = WebTestScenario()
}
