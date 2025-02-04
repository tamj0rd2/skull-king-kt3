package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCaseContract
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCaseContract
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCaseContract
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCaseContract
import com.tamj0rd2.skullking.application.port.input.StartGameUseCaseContract
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD

// NOTE: if/when this test takes too long for fast feedback, I can put it into its own
// test gradle task. It's not something that's crucial to run before each push.
// When I have a pipeline, I can just run it there instead.
@Execution(SAME_THREAD)
class WebServerTest :
    CreateNewLobbyUseCaseContract,
    JoinALobbyUseCaseContract,
    StartGameUseCaseContract,
    PlaceABidUseCaseContract,
    PlayACardUseCaseContract {
    override val propertyTestIterations = 1

    private companion object {
        val webServer = WebServer(port = Main.DEFAULT_PORT)
    }

    @AutoClose
    override val scenario = WebTestScenario(webServer)
}
