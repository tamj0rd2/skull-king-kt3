package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.EndToEndTestContract
import com.tamj0rd2.skullking.Player
import com.tamj0rd2.skullking.adapters.inmemory.inMemory
import com.tamj0rd2.skullking.domain.game.PlayerId
import org.junit.platform.commons.annotation.Testable

@Testable
class ApplicationEndToEndTest : EndToEndTestContract {
    private val application = UseCases.createFrom(OutputPorts.inMemory())

    override fun createPlayerActor(name: String): Player {
        return Player(PlayerId.of(name), application)
    }
}
