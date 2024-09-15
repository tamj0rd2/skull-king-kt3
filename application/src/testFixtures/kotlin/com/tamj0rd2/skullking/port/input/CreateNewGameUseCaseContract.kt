package com.tamj0rd2.skullking.port.input

import com.tamj0rd2.skullking.ApplicationDriver
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.assertions.isSuccess

abstract class CreateNewGameUseCaseContract {
    protected abstract fun newDriver(): ApplicationDriver

    @Test
    fun `can create a new game`() {
        val driver = newDriver()
        expectCatching { driver(CreateNewGameCommand).gameId }.isSuccess()
    }
}
