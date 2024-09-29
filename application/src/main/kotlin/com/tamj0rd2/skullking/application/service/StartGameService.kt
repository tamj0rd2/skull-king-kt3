package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameOutput
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.domain.model.GameUpdate

class StartGameService(
    private val gameUpdateNotifier: GameUpdateNotifier,
) : StartGameUseCase {
    override fun invoke(command: StartGameCommand): StartGameOutput {
        gameUpdateNotifier.broadcast(GameUpdate.GameStarted)
        return StartGameOutput
    }
}
