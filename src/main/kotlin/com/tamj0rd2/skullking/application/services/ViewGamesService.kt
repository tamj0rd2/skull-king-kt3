package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.input.GameListItem
import com.tamj0rd2.skullking.application.ports.input.ViewGamesInput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesOutput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesUseCase
import com.tamj0rd2.skullking.application.ports.output.FindGamesPort

class ViewGamesService(private val findGamesPort: FindGamesPort) : ViewGamesUseCase {
    override fun execute(input: ViewGamesInput): ViewGamesOutput {
        return ViewGamesOutput(games = listOf(GameListItem))
    }
}
