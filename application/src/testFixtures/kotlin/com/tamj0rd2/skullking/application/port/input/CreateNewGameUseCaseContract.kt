package com.tamj0rd2.skullking.application.port.input

import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.assertions.isSuccess

abstract class CreateNewGameUseCaseContract {
    protected abstract val scenario: TestScenario

    @Test
    fun `can create a new game`() {
        val player = scenario.newPlayer()
        expectCatching { player.createsAGame() }.isSuccess()
    }
}
