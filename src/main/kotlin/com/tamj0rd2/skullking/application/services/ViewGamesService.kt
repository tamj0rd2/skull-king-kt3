package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.input.GamesListItem
import com.tamj0rd2.skullking.application.ports.input.ViewGamesInput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesOutput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesUseCase
import com.tamj0rd2.skullking.application.ports.output.LoadAllGamesPort

class ViewGamesService(private val loadAllGamesPort: LoadAllGamesPort) : ViewGamesUseCase {
    override fun execute(input: ViewGamesInput): ViewGamesOutput {
        val games = loadAllGamesPort.loadAll().map { GamesListItem(id = it.id) }

        return ViewGamesOutput(games = games)
    }
}
