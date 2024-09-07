package com.tamj0rd2.skullking.port.input

import com.tamj0rd2.skullking.domain.service.ViewPlayerGameStateService
import com.tamj0rd2.skullking.port.output.GameEventsInMemoryAdapter
import com.tamj0rd2.skullking.port.output.GameEventsPort

class ViewPlayerGameStateServiceTest : ViewPlayerGameStateUseCaseContract() {
    override val gameEventsPort: GameEventsPort = GameEventsInMemoryAdapter()
    override val useCase: ViewPlayerGameStateUseCase = ViewPlayerGameStateService(gameEventsPort)
}
