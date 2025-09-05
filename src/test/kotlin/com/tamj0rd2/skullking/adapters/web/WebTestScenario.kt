package com.tamj0rd2.skullking.adapters.web

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.tamj0rd2.skullking.Player
import com.tamj0rd2.skullking.adapters.configuration.forTesting
import com.tamj0rd2.skullking.application.ports.input.TestScenario
import com.tamj0rd2.skullking.application.ports.output.OutputPorts
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.testsupport.eventually
import org.junit.jupiter.api.AutoClose

class WebTestScenario : TestScenario, AutoCloseable {
    private val port = 8123

    private val webServer = WebServer(outputPorts = OutputPorts.forTesting(), port = port).also { it.start() }

    @AutoClose private val playwright = Playwright.create()
    private val browser = playwright.chromium().launch(BrowserType.LaunchOptions())

    override fun close() {
        browser.close()
        webServer.close()
    }

    override fun createPlayerActor(name: String): Player {
        val page = browser.newContext().newPage()
        page.setDefaultNavigationTimeout(2000.0)
        page.setDefaultTimeout(1000.0)

        val webClient = WebClient(page, "http://localhost:$port")

        return Player(
            id = PlayerId.of(value = name),
            useCases = webClient.useCases(),
            deriveGameState = webClient,
            eventually = { block -> eventually(5000, block) },
        )
    }
}
