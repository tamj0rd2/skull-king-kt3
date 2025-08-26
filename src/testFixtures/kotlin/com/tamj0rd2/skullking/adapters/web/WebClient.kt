package com.tamj0rd2.skullking.adapters.web

import com.microsoft.playwright.Page
import com.tamj0rd2.skullking.application.UseCases
import com.tamj0rd2.skullking.application.ports.input.CreateGameOutput
import com.tamj0rd2.skullking.application.ports.input.GameListItem
import com.tamj0rd2.skullking.application.ports.input.JoinGameOutput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesOutput
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.values.random

class WebClient private constructor(private val page: Page) {
    private fun useCases(): UseCases {
        return UseCases(
            createGameUseCase = { CreateGameOutput(GameId.random()) },
            viewGamesUseCase = {
                ViewGamesOutput(listOf(GameListItem(GameId.random(), PlayerId("Cammy"))))
            },
            joinGameUseCase = { JoinGameOutput },
        )
    }

    companion object {
        fun UseCases.Companion.overHttp(page: Page): UseCases {
            return WebClient(page).useCases()
        }
    }
}
