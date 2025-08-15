package com.tamj0rd2.skullking

import com.tamj0rd2.skullking.application.Application
import com.tamj0rd2.skullking.application.ports.input.CreateGameInput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesInput
import com.tamj0rd2.skullking.domain.game.PlayerId
import strikt.api.expectThat
import strikt.assertions.single

class Player(val id: PlayerId, val application: Application) {
    fun `creates a game`() {
        application.createGameUseCase.execute(CreateGameInput(id))
    }

    fun `sees that the game has been created`() {
        val games = application.viewGamesUseCase.execute(ViewGamesInput).games
        expectThat(games).single()
    }
}
