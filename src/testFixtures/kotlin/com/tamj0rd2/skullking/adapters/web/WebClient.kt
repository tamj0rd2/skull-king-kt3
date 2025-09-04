package com.tamj0rd2.skullking.adapters.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole.BUTTON
import com.tamj0rd2.skullking.Player.DeriveGameState
import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import com.tamj0rd2.skullking.application.ports.input.CreateGameOutput
import com.tamj0rd2.skullking.application.ports.input.GameListItem
import com.tamj0rd2.skullking.application.ports.input.JoinGameOutput
import com.tamj0rd2.skullking.application.ports.input.StartGameOutput
import com.tamj0rd2.skullking.application.ports.input.UseCases
import com.tamj0rd2.skullking.application.ports.input.ViewGamesOutput
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.domain.game.RoundNumber

internal class WebClient(private val page: Page, private val baseUrl: String) : DeriveGameState {
    init {
        page.onLoad { it.installHtmxSupport() }
    }

    fun useCases(): UseCases {
        return UseCases(
            createGameUseCase = { useCaseInput ->
                page.navigate("$baseUrl/games")
                page.waitingForHtmx(http = true) { page.getByRole(BUTTON).withText("Create Game").click() }
                page.getByLabel("Player ID").fill(useCaseInput.playerId.value)
                page.waitingForHtmx(http = true, ws = true) { page.getByRole(BUTTON).withText("Create Game").click() }

                CreateGameOutput
            },
            viewGamesUseCase = {
                page.navigate("$baseUrl/games")
                ViewGamesOutput(games = parseGamesList())
            },
            joinGameUseCase = { useCaseInput ->
                val gameElement = page.findByAttribute("data-game-id", GameId.show(useCaseInput.gameId)).single()

                page.waitingForHtmx(http = true) { gameElement.getByRole(BUTTON).withText("Join Game").click() }
                page.getByLabel("Player ID").fill(useCaseInput.playerId.value)
                page.waitingForHtmx(http = true, ws = true) { page.getByRole(BUTTON).withText("Join Game").click() }

                JoinGameOutput
            },
            startGameUseCase = { useCaseInput ->
                page.waitingForHtmx(ws = true) { page.getByRole(BUTTON).withText("Start Game").click() }

                StartGameOutput
            },
        )
    }

    override fun current(): PlayerSpecificGameState? {
        val gameElement = page.locator("#game").firstOrNull() ?: return null
        val gameId = gameElement.getAttribute("data-game-id").let(GameId::parse)

        val players = gameElement.locator("#players li").all().map { playerElement -> playerElement.textContent().let(PlayerId::parse) }
        val roundNumber = gameElement.getByTestId("round-number").firstOrNull()?.textContent()?.toInt()?.let(RoundNumber::fromInt)

        return PlayerSpecificGameState(gameId = gameId, players = players, roundNumber = roundNumber)
    }

    private fun Page.findByAttribute(attribute: String, value: String) = locator("[$attribute='$value']").all()

    private fun parseGamesList(): List<GameListItem> =
        page.locator("#games > div").all().map { gameElement ->
            GameListItem(
                id = gameElement.getAttribute("data-game-id").let(GameId::parse),
                host = gameElement.getAttribute("data-host-id").let { PlayerId.parse(it) },
            )
        }

    companion object {
        private const val htmxSettled = "htmxHasSettled"
        private const val htmxWsSettled = "htmxWsHasSettled"

        private fun Page.installHtmxSupport() {
            evaluate("window.$htmxSettled = false; window.addEventListener('htmx:afterSettle', () => window.$htmxSettled = true);")
            evaluate("window.$htmxWsSettled = false; window.addEventListener('htmx:wsAfterMessage', () => window.$htmxWsSettled = true);")
        }

        private fun Page.waitingForHtmx(http: Boolean = false, ws: Boolean = false, action: () -> Unit) {
            require(http || ws) { "Must specify to wait for at least one of http or ws" }

            if (http) evaluate("window.$htmxSettled = false")
            if (ws) evaluate("window.$htmxWsSettled = false")

            action()

            if (http) waitForFunction("window.$htmxSettled === true")
            if (ws) waitForFunction("window.$htmxWsSettled === true")
        }

        private fun Locator.withText(text: String): Locator = filter(Locator.FilterOptions().setHasText(text))

        private fun Locator.firstOrNull(): Locator? = if (count() > 0) first() else null
    }
}
