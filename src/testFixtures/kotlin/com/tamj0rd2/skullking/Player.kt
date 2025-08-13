package com.tamj0rd2.skullking

import com.tamj0rd2.skullking.application.Application
import com.tamj0rd2.skullking.application.ports.input.CreateGameInput
import com.tamj0rd2.skullking.application.ports.input.JoinGameInput

class Player(val application: Application) {
    fun `creates a game`() {
        application.createGameUseCase.execute(CreateGameInput)
    }

    fun `joins a game`() {
        application.joinGameUseCase.execute(JoinGameInput)
    }
}
