package com.tamj0rd2.skullking.adapters.inmemory

import com.tamj0rd2.skullking.application.ports.output.GameRepository
import com.tamj0rd2.skullking.application.ports.output.GameRepositoryContract
import org.junit.platform.commons.annotation.Testable

@Testable
class InMemoryGameRepositoryTests : GameRepositoryContract {
    override val gameRepository: GameRepository = InMemoryGameRepository()
}
