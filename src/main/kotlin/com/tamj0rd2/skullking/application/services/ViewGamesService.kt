package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.input.GameListItem
import com.tamj0rd2.skullking.application.ports.input.ViewGamesInput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesOutput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesUseCase
import com.tamj0rd2.skullking.application.repositories.GameRepository

class ViewGamesService(private val gameRepository: GameRepository) : ViewGamesUseCase {
    override fun execute(input: ViewGamesInput): ViewGamesOutput {
        val games = gameRepository.findAll().map { GameListItem(id = it.id, host = it.creator) }
        return ViewGamesOutput(games = games)
    }
}
