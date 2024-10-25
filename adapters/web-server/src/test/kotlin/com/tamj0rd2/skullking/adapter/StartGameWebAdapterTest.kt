package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.port.input.StartGameUseCaseContract

class StartGameWebAdapterTest : StartGameUseCaseContract() {
    override val scenario = WebTestScenario()
}
