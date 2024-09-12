package com.tamj0rd2.skullking.domain.port

import com.tamj0rd2.skullking.port.output.GameEventsInMemoryAdapter
import com.tamj0rd2.skullking.port.output.GameEventsPort
import com.tamj0rd2.skullking.port.output.GameEventsPortContract

class GameEventsInMemoryAdapterTests : GameEventsPortContract() {
    override val gameEventsPort: GameEventsPort = GameEventsInMemoryAdapter()
}
