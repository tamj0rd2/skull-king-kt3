package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameRepositoryContract

class GameRepositoryInMemoryAdapterTests : GameRepositoryContract() {
    override val gameRepository: GameRepository = GameRepositoryInMemoryAdapter()
}
