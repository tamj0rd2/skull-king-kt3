package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.EndToEndTestContract
import com.tamj0rd2.skullking.Player
import com.tamj0rd2.skullking.Player.DeriveGameState
import com.tamj0rd2.skullking.Player.GameState
import com.tamj0rd2.skullking.adapters.inmemory.inMemory
import com.tamj0rd2.skullking.application.ports.GameNotification
import com.tamj0rd2.skullking.domain.game.PlayerId
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.platform.commons.annotation.Testable

@Testable
class ApplicationEndToEndTest : EndToEndTestContract {
    private val application = UseCases.createFrom(OutputPorts.inMemory())

    override fun createPlayerActor(name: String): Player {
        return Player(
            id = PlayerId.of(name),
            useCases = application,
            deriveGameState =
                object : DeriveGameState {
                    private val receivedNotifications = CopyOnWriteArrayList<GameNotification>()

                    override fun receive(gameNotification: GameNotification) {
                        receivedNotifications.add(gameNotification)
                    }

                    override fun current(): GameState {
                        return receivedNotifications.fold(GameState(), GameState::apply)
                    }
                },
        )
    }
}
