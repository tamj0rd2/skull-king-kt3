package com.tamj0rd2.skullking.domain.port

import com.tamj0rd2.skullking.port.output.GameEventsInMemoryAdapter
import com.tamj0rd2.skullking.port.output.GameEventsPortContract
import com.tamj0rd2.skullking.port.output.GameRepository

class GameEventsInMemoryAdapterTests : GameEventsPortContract() {
    override val gameEventsPort: GameRepository = GameEventsInMemoryAdapter()
}
