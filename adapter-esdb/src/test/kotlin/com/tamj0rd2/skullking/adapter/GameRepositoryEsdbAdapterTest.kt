package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameRepositoryContract
import net.jqwik.api.PropertyDefaults
import net.jqwik.api.ShrinkingMode
import org.junit.jupiter.api.Timeout

@Timeout(1)
@PropertyDefaults(tries = 50, shrinking = ShrinkingMode.BOUNDED)
class GameRepositoryEsdbAdapterTest : GameRepositoryContract() {
    override val gameRepository: GameRepository = GameRepositoryEsdbAdapter()
}
