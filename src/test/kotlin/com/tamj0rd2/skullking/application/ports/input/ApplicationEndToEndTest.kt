package com.tamj0rd2.skullking.application.ports.input

import com.tamj0rd2.skullking.EndToEndTestContract
import com.tamj0rd2.skullking.Player
import com.tamj0rd2.skullking.application.Application
import com.tamj0rd2.skullking.createOutputPortsForTesting
import com.tamj0rd2.skullking.domain.game.PlayerId
import org.junit.platform.commons.annotation.Testable

@Testable
class ApplicationEndToEndTest : EndToEndTestContract {
    private val application = Application.create(createOutputPortsForTesting())

    override fun createPlayerActor(name: String): Player {
        return Player(PlayerId.of(name), application)
    }
}
