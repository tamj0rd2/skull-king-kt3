package com.tamj0rd2.skullking.application.ports.input

import com.tamj0rd2.skullking.EndToEndTestContract
import com.tamj0rd2.skullking.Player
import com.tamj0rd2.skullking.application.Application
import org.junit.platform.commons.annotation.Testable

@Testable
class ApplicationEndToEndTest : EndToEndTestContract {
    private val application = Application.create()

    override fun createPlayerActor(): Player {
        return Player(application)
    }
}
