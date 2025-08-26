package com.tamj0rd2.skullking.adapters.web

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
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebEndToEndTest : EndToEndTestContract {
    @Suppress("unused")
    @AutoClose
    private val webServer =
        WebServer(outputPorts = OutputPorts.inMemory(), port = 8123).also { it.start() }

    @AutoClose private val playwright = Playwright.create()
    private val browser = playwright.chromium().launch()

    override fun createPlayerActor(name: String): Player {
        val page = browser.newContext().newPage()
        return Player(
            id = PlayerId(value = name),
            useCases = UseCases.overHttp(page),
            deriveGameState =
                object : DeriveGameState {
                    override fun current(): GameState {
                        TODO("Not yet implemented")
                    }

                    override fun receive(gameNotification: GameNotification) {
                        TODO("Not yet implemented")
                    }
                },
        )
    }
}
