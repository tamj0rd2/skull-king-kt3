package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameRepositoryContract

class GameRepositoryEsdbAdapterTest : GameRepositoryContract() {
    override val gameRepository: GameRepository = GameRepositoryEsdbAdapter()
}
