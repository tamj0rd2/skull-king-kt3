package com.tamj0rd2.skullking.application.ports.input

import com.tamj0rd2.skullking.application.Application
import com.tamj0rd2.skullking.createOutputPortsForTesting
import org.junit.platform.commons.annotation.Testable

@Testable
class CreateGamesServiceTest : CreateGameUseCaseTest {
    override val application = Application.create(createOutputPortsForTesting())
}
