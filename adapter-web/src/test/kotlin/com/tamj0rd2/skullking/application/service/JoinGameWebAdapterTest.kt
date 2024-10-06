package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.JoinGameGameUseCaseContract
import org.junit.jupiter.api.Timeout

@Timeout(2)
class JoinGameWebAdapterTest : JoinGameGameUseCaseContract() {
    override val scenario = WebTestScenario()
}
