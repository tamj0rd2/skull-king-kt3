package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameRepositoryContract
import io.kotest.property.PropertyTesting
import org.junit.jupiter.api.Timeout

@Timeout(2)
class GameRepositoryEsdbAdapterTest : GameRepositoryContract() {
    override val gameRepository: GameRepository = GameRepositoryEsdbAdapter()

    init {
        PropertyTesting.defaultIterationCount = 50
    }
}
