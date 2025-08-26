package com.tamj0rd2.skullking.adapters.web

import com.microsoft.playwright.Page
import com.tamj0rd2.skullking.application.UseCases
import com.tamj0rd2.skullking.application.ports.input.CreateGameOutput
import com.tamj0rd2.skullking.application.ports.input.GameListItem
import com.tamj0rd2.skullking.application.ports.input.JoinGameOutput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesOutput
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class WebClient private constructor(private val page: Page, private val baseUrl: String) {
    private fun useCases(): UseCases {
        return UseCases(
            createGameUseCase = { useCaseInput ->
                val response = page.navigate("$baseUrl/games")
                expectThat(response.status()).isEqualTo(200)

                page.getByPlaceholder("Player ID").fill(useCaseInput.playerId.value)
                page.waitingForHtmx { page.getByText("Create Game").click() }

                val gameId = parseGamesList().single { it.host == useCaseInput.playerId }.id
                CreateGameOutput(gameId = gameId)
            },
            viewGamesUseCase = {
                val response = page.navigate("$baseUrl/games")
                expectThat(response.status()).isEqualTo(200)

                ViewGamesOutput(games = parseGamesList())
            },
            joinGameUseCase = { JoinGameOutput },
        )
    }

    private fun parseGamesList(): List<GameListItem> {
        val games =
            (0 until page.locator("#games li").count()).map { index ->
                val gameElement = page.locator("#games li").nth(index)

                GameListItem(
                    id = gameElement.getAttribute("data-game-id").let(GameId::parse),
                    host = gameElement.getAttribute("data-host-id").let { PlayerId.parse(it) },
                )
            }
        return games
    }

    init {
        page.onLoad { it.installHtmxSupport() }
    }

    private val htmxSettled = "htmxHasSettled"

    private fun Page.installHtmxSupport() {
        evaluate(
            "window.$htmxSettled = false; window.addEventListener('htmx:afterSettle', () => window.$htmxSettled = true);"
        )
    }

    private fun Page.waitingForHtmx(action: Page.() -> Unit) {
        evaluate("window.$htmxSettled = false")
        action()
        waitForFunction("window.$htmxSettled === true")
    }

    companion object {
        fun UseCases.Companion.overHttp(page: Page, baseUrl: String): UseCases {
            return WebClient(page, baseUrl).useCases()
        }
    }
}
