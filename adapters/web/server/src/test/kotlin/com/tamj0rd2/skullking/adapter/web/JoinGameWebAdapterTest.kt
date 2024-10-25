package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCaseContract

class JoinGameWebAdapterTest : JoinAGameUseCaseContract() {
    override val scenario = WebTestScenario()
}
