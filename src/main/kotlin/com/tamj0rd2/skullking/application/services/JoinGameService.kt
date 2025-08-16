package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.GameNotification
import com.tamj0rd2.skullking.application.ports.input.JoinGameInput
import com.tamj0rd2.skullking.application.ports.input.JoinGameOutput
import com.tamj0rd2.skullking.application.ports.input.JoinGameUseCase

class JoinGameService : JoinGameUseCase {
    override fun execute(input: JoinGameInput): JoinGameOutput {
        input.receiveGameNotification.receive(GameNotification.PlayerJoined(input.playerId))

        return JoinGameOutput
    }
}
