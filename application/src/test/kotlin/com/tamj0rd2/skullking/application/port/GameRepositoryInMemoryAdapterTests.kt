package com.tamj0rd2.skullking.application.port

import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameRepositoryContract
import com.tamj0rd2.skullking.application.port.output.GameRepositoryInMemoryAdapter

class GameRepositoryInMemoryAdapterTests : GameRepositoryContract() {
    override val gameRepository: GameRepository = GameRepositoryInMemoryAdapter()
}
