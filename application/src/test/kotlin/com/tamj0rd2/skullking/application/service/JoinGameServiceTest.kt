package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.ApplicationDomainDriver
import com.tamj0rd2.skullking.application.port.input.InMemoryEventListeners
import com.tamj0rd2.skullking.application.port.input.JoinGameGameUseCaseContract
import com.tamj0rd2.skullking.application.port.input.roles.PlayerRole
import com.tamj0rd2.skullking.application.port.output.GameRepositoryInMemoryAdapter

class JoinGameServiceTest : JoinGameGameUseCaseContract() {
    private val listeners = InMemoryEventListeners()

    private val driver =
        ApplicationDomainDriver(
            gameRepository = GameRepositoryInMemoryAdapter(listeners),
        )

    override fun newPlayerRole() = PlayerRole(driver).also { listeners.register(it) }
}
