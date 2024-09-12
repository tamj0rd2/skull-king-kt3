package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.port.output.GameEventsPort
import com.tamj0rd2.skullking.port.output.GameEventsPortContract

class GameEventsEsdbAdapterTest : GameEventsPortContract() {
    override val gameEventsPort: GameEventsPort = GameEventsEsdbAdapter()
}
