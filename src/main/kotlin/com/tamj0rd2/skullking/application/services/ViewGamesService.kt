package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.input.ViewGamesInput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesOutput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesUseCase

class ViewGamesService : ViewGamesUseCase {
    override fun execute(input: ViewGamesInput): ViewGamesOutput {
        return ViewGamesOutput(games = emptyList())
    }
}
