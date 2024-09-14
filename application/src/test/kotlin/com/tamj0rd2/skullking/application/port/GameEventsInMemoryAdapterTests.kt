package com.tamj0rd2.skullking.domain.port

import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.port.output.GameEventsInMemoryAdapter
import com.tamj0rd2.skullking.port.output.GameRepositoryContract

class GameEventsInMemoryAdapterTests : GameRepositoryContract() {
    override val gameRepository: GameRepository = GameEventsInMemoryAdapter()
}
