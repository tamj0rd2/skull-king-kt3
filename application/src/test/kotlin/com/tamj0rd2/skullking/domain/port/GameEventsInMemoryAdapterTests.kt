package com.tamj0rd2.skullking.domain.port

import com.tamj0rd2.skullking.port.output.GameEventsInMemoryAdapter
import com.tamj0rd2.skullking.port.output.GameRepository
import com.tamj0rd2.skullking.port.output.gameRepositoryContract

class GameEventsInMemoryAdapterTests : gameRepositoryContract() {
    override val gameRepository: GameRepository = GameEventsInMemoryAdapter()
}
