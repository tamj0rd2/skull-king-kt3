package com.tamj0rd2.skullking.application.port.input

import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.assertions.isSuccess

abstract class CreateNewGameGameUseCaseContract : GameUseCaseContract {
    @Test
    fun `can create a new game`() {
        val player = newPlayerRole()
        expectCatching { player.createsAGame() }.isSuccess()
    }
}
