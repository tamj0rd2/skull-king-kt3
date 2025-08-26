package com.tamj0rd2.skullking.adapters.web

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.tamj0rd2.skullking.EndToEndTestContract
import com.tamj0rd2.skullking.Player
import com.tamj0rd2.skullking.Player.DeriveGameState
import com.tamj0rd2.skullking.Player.GameState
import com.tamj0rd2.skullking.adapters.inmemory.inMemory
import com.tamj0rd2.skullking.adapters.web.WebClient.Companion.overHttp
import com.tamj0rd2.skullking.application.OutputPorts
import com.tamj0rd2.skullking.application.UseCases
import com.tamj0rd2.skullking.application.ports.GameNotification
import com.tamj0rd2.skullking.domain.game.PlayerId
import java.lang.management.ManagementFactory
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebEndToEndTest : EndToEndTestContract {
    private val port = 8123

    @AutoClose
    @Suppress("unused")
    private val webServer =
        WebServer(outputPorts = OutputPorts.inMemory(), port = port).also { it.start() }

    @AutoClose private val playwright = Playwright.create()
    private val browser =
        playwright
            .chromium()
            .launch(BrowserType.LaunchOptions().setHeadless(!isDebuggingInIntellij()))

    override fun createPlayerActor(name: String): Player {
        val page = browser.newContext().newPage()
        return Player(
            id = PlayerId(value = name),
            useCases = UseCases.overHttp(page, baseUrl = "http://localhost:$port"),
            deriveGameState =
                object : DeriveGameState {
                    override fun current(): GameState {
                        return GameState()
                    }

                    override fun receive(gameNotification: GameNotification) {
                        TODO("Not yet implemented")
                    }
                },
        )
    }

    private fun isDebuggingInIntellij(): Boolean {
        val args = ManagementFactory.getRuntimeMXBean().inputArguments
        return args.any { it.contains("idea", true) } && args.any { it.contains("jdwp", true) }
    }
}
