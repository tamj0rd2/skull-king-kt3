package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.port.output.GameRepositoryContract
import io.kotest.property.PropertyTesting

class GameRepositoryEsdbAdapterTest : GameRepositoryContract() {
    companion object {
        // here so that I only need to deal with establishing a connection once
        val repo = GameRepositoryEsdbAdapter()
    }

    override val gameRepository: GameRepositoryEsdbAdapter = repo

    init {
        PropertyTesting.defaultIterationCount = 10
    }
}
