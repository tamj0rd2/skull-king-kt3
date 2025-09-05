package com.tamj0rd2.skullking.application.ports.input

import com.tamj0rd2.skullking.Player

interface HasTestScenario {
    val scenario: TestScenario
}

interface TestScenario {
    fun createPlayerActor(name: String): Player
}
