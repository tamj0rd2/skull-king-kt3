package com.tamj0rd2.skullking.adapters.web

import com.microsoft.playwright.Page
import com.tamj0rd2.skullking.application.UseCases

class WebClient private constructor(private val page: Page) {
    private fun useCases(): UseCases {
        return UseCases(
            createGameUseCase = { TODO() },
            viewGamesUseCase = { TODO() },
            joinGameUseCase = { TODO() },
        )
    }

    companion object {
        fun UseCases.Companion.overHttp(page: Page): UseCases {
            return WebClient(page).useCases()
        }
    }
}
