package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.port.output.gameRepositoryContract

class GameEventsEsdbAdapterTest : gameRepositoryContract() {
    override val gameRepository: GameRepository = GameEventsEsdbAdapter()
}
