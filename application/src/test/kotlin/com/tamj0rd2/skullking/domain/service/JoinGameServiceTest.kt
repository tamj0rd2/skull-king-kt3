package com.tamj0rd2.skullking.domain.service

import com.tamj0rd2.skullking.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.port.input.JoinGameUseCaseContract
import com.tamj0rd2.skullking.port.output.GameEventsInMemoryAdapter
import com.tamj0rd2.skullking.port.output.GameEventsPort

class JoinGameServiceTest : JoinGameUseCaseContract() {
    override val gameEventsPort: GameEventsPort = GameEventsInMemoryAdapter()
    override val useCase: JoinGameUseCase = JoinGameService(gameEventsPort)
}
