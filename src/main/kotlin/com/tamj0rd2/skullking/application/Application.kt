package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.ports.input.CreateGameUseCase
import com.tamj0rd2.skullking.application.ports.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.ports.input.ViewGamesUseCase
import com.tamj0rd2.skullking.application.services.CreateGameService
import com.tamj0rd2.skullking.application.services.JoinGameService
import com.tamj0rd2.skullking.application.services.ViewGamesService

data class Application(
    val createGameUseCase: CreateGameUseCase,
    val joinGameUseCase: JoinGameUseCase,
    val viewGamesUseCase: ViewGamesUseCase,
) {
    companion object {
        fun create(outputPorts: OutputPorts) =
            Application(
                createGameUseCase = CreateGameService(outputPorts.saveGamePort),
                joinGameUseCase = JoinGameService(),
                viewGamesUseCase = ViewGamesService(),
            )
    }
}
