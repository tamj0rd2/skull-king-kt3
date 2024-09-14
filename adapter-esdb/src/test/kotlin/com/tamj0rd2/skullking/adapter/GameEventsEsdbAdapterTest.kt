package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.port.output.GameEventsPortContract
import com.tamj0rd2.skullking.port.output.GameRepository

class GameEventsEsdbAdapterTest : GameEventsPortContract() {
    override val gameEventsPort: GameRepository = GameEventsEsdbAdapter()
}
