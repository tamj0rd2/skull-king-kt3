package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.ports.input.CreateGameUseCase
import com.tamj0rd2.skullking.application.services.CreateGameService

data class Application(val createGameUseCase: CreateGameUseCase) {
    companion object {
        fun create() = Application(createGameUseCase = CreateGameService())
    }
}
