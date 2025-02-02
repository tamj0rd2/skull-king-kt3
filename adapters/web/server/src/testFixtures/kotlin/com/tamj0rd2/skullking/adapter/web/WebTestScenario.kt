package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.SkullKingApplication
import com.tamj0rd2.skullking.application.port.input.testsupport.PlayerRole
import com.tamj0rd2.skullking.application.port.input.testsupport.TestScenario
import com.tamj0rd2.skullking.application.usingTestDoublesByDefault
import org.http4k.core.Uri

class WebTestScenario(
    webServer: WebServer = WebServer(application = SkullKingApplication.usingTestDoublesByDefault()),
) : TestScenario,
    AutoCloseable {
    private val server = webServer.start()

    override fun close() {
        server.close()
        clients.forEach { runCatching { it.close() } }
    }

    private val clients = mutableListOf<SkullKingWebClient>()

    override fun newPlayer(): PlayerRole {
        val webClient = SkullKingWebClient(Uri.of("ws://localhost:${server.port()}")).also { clients.add(it) }
        return PlayerRole(webClient)
    }
}
