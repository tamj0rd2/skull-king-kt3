package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.port.input.JoinGameUseCaseContract

class JoinGameWebAdapterTest : JoinGameUseCaseContract() {
    override val scenario = WebTestScenario()
}