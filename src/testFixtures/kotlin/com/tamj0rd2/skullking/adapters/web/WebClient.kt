package com.tamj0rd2.skullking.adapters.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole.BUTTON
import com.tamj0rd2.skullking.Player.DeriveGameState
import com.tamj0rd2.skullking.application.UseCases
import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import com.tamj0rd2.skullking.application.ports.input.CreateGameOutput
import com.tamj0rd2.skullking.application.ports.input.GameListItem
import com.tamj0rd2.skullking.application.ports.input.JoinGameOutput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesOutput
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class WebClient(private val page: Page, private val baseUrl: String) : DeriveGameState {
    init {
        page.onLoad { it.installHtmxSupport() }
    }

    fun useCases(): UseCases {
        return UseCases(
            createGameUseCase = { useCaseInput ->
                val response = page.navigate("$baseUrl/games")
                expectThat(response.status()).isEqualTo(200)

                page.waitingForHtmx { page.getByRole(BUTTON).withText("Create Game").click() }
                page.getByLabel("Player ID").fill(useCaseInput.playerId.value)
                page.waitingForHtmx { page.getByRole(BUTTON).withText("Create Game").click() }

                CreateGameOutput
            },
            viewGamesUseCase = {
                val response = page.navigate("$baseUrl/games")
                expectThat(response.status()).isEqualTo(200)

                ViewGamesOutput(games = parseGamesList())
            },
            joinGameUseCase = { useCaseInput ->
                val gameElement =
                    page.findByAttribute("data-game-id", GameId.show(useCaseInput.gameId)).single()

                page.waitingForHtmx { gameElement.getByRole(BUTTON).withText("Join Game").click() }
                page.getByLabel("Player ID").fill(useCaseInput.playerId.value)
                page.waitingForHtmx { page.getByRole(BUTTON).withText("Join Game").click() }

                JoinGameOutput
            },
        )
    }

    override fun current(): PlayerSpecificGameState {
        val players =
            page.locator("#players li").all().map { playerElement ->
                playerElement.textContent().let(PlayerId::parse)
            }

        return PlayerSpecificGameState(players = players)
    }

    private fun Page.findByAttribute(attribute: String, value: String) =
        locator("[$attribute='$value']").all()

    private fun parseGamesList(): List<GameListItem> =
        page.locator("#games > div").all().map { gameElement ->
            GameListItem(
                id = gameElement.getAttribute("data-game-id").let(GameId::parse),
                host = gameElement.getAttribute("data-host-id").let { PlayerId.parse(it) },
            )
        }

    companion object {
        private const val htmxSettled = "htmxHasSettled"

        private fun Page.installHtmxSupport() {
            evaluate(
                "window.$htmxSettled = false; window.addEventListener('htmx:afterSettle', () => window.$htmxSettled = true);"
            )
        }

        private fun Page.waitingForHtmx(action: () -> Unit) {
            evaluate("window.$htmxSettled = false")
            action()
            waitForFunction("window.$htmxSettled === true")
        }

        private fun Locator.withText(text: String): Locator =
            filter(Locator.FilterOptions().setHasText(text))
    }
}
