package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.JoinGameUseCaseContract

class JoinGameWebAdapterTest : JoinGameUseCaseContract() {
    override val scenario = WebTestScenario()
}
