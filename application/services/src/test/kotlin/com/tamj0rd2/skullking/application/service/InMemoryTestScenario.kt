package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.PlayerRole
import com.tamj0rd2.skullking.application.port.input.TestScenario
import com.tamj0rd2.skullking.application.usingTestDoublesByDefault

class InMemoryTestScenario : TestScenario {
    private val driver = SkullKingApplication.usingTestDoublesByDefault()

    override fun newPlayer() = PlayerRole(driver)
}
