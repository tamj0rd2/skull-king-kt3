package com.tamj0rd2.skullking.application.ports.input

import com.tamj0rd2.skullking.application.Application
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.single

interface CreateGameUseCaseContract {
    val application: Application

    @Test
    fun `newly created games can be seen in the list of games`() {
        val createdGameId = application.createGameUseCase.execute(CreateGameInput).gameId

        val output = application.viewGamesUseCase.execute(ViewGamesInput)

        expectThat(output.games).single().get { id }.isEqualTo(createdGameId)
    }
}
