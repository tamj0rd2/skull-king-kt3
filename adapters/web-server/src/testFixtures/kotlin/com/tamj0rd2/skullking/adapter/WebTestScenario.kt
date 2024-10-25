package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.adapter.web.WebServer
import com.tamj0rd2.skullking.application.port.input.PlayerRole
import com.tamj0rd2.skullking.application.port.input.TestScenario
import com.tamj0rd2.skullking.application.service.SkullKingApplication
import com.tamj0rd2.skullking.application.usingTestDoublesByDefault
import come.tamj0rd2.skullking.adapter.SkullKingWebClient
import org.http4k.core.Uri
import org.junit.jupiter.api.AutoClose

class WebTestScenario : TestScenario {
    @AutoClose
    private val server = WebServer.createServer(SkullKingApplication.usingTestDoublesByDefault()).start()

    override fun newPlayer() = PlayerRole(SkullKingWebClient(Uri.of("ws://localhost:${server.port()}")))
}
