package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.SkullKingApplication
import com.tamj0rd2.skullking.application.port.input.testsupport.PlayerRole
import com.tamj0rd2.skullking.application.port.input.testsupport.TestScenario
import com.tamj0rd2.skullking.application.usingTestDoublesByDefault

class InMemoryTestScenario : TestScenario {
    private val driver = SkullKingApplication.usingTestDoublesByDefault()

    override fun newPlayer() = PlayerRole(driver)
}
