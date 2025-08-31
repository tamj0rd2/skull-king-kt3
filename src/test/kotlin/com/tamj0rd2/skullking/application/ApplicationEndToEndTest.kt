package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.EndToEndTestContract
import com.tamj0rd2.skullking.Player
import com.tamj0rd2.skullking.Player.DeriveGameState
import com.tamj0rd2.skullking.adapters.inmemory.inMemory
import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import com.tamj0rd2.skullking.application.ports.ReceiveGameNotification
import com.tamj0rd2.skullking.application.ports.input.UseCases
import com.tamj0rd2.skullking.application.ports.output.OutputPorts
import com.tamj0rd2.skullking.application.services.using
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.testsupport.eventually
import java.util.concurrent.atomic.AtomicReference
import org.junit.platform.commons.annotation.Testable

@Testable
class ApplicationEndToEndTest : EndToEndTestContract {
    private val application = UseCases.using(OutputPorts.inMemory())

    override fun createPlayerActor(name: String): Player {
        return Player(
            id = PlayerId.of(name),
            useCases = application,
            deriveGameState =
                object : DeriveGameState, ReceiveGameNotification {
                    private val latestState = AtomicReference<PlayerSpecificGameState?>(null)

                    override fun receive(playerSpecificGameState: PlayerSpecificGameState) {
                        latestState.set(playerSpecificGameState)
                    }

                    override fun current(): PlayerSpecificGameState? {
                        return latestState.get()
                    }
                },
            eventually = { block -> eventually(1000, block) },
        )
    }
}
