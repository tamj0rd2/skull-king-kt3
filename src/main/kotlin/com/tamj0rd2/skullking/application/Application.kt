package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.ports.input.CreateGameUseCase
import com.tamj0rd2.skullking.application.ports.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.services.CreateGameService
import com.tamj0rd2.skullking.application.services.JoinGameService

data class Application(
    val createGameUseCase: CreateGameUseCase,
    val joinGameUseCase: JoinGameUseCase,
) {
    companion object {
        fun create() =
            Application(
                createGameUseCase = CreateGameService(),
                joinGameUseCase = JoinGameService(),
            )
    }
}
