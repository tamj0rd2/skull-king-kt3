package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.EndToEndTestContract
import com.tamj0rd2.skullking.Player
import com.tamj0rd2.skullking.Player.DeriveGameState
import com.tamj0rd2.skullking.adapters.inmemory.inMemory
import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import com.tamj0rd2.skullking.application.ports.ReceiveGameNotification
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.testsupport.eventually
import java.util.concurrent.atomic.AtomicReference
import org.junit.platform.commons.annotation.Testable
import strikt.api.expectThat
import strikt.assertions.isNotNull

@Testable
class ApplicationEndToEndTest : EndToEndTestContract {
    private val application = UseCases.createFrom(OutputPorts.inMemory())

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

                    override fun current(): PlayerSpecificGameState {
                        return expectThat(latestState.get()).isNotNull().subject
                    }
                },
            eventually = { block -> eventually(1000, block) },
        )
    }
}
