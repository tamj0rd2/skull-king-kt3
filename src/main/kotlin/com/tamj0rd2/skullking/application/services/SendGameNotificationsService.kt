package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import com.tamj0rd2.skullking.application.ports.SendGameNotificationPort
import com.tamj0rd2.skullking.application.ports.output.GameEventSubscriber
import com.tamj0rd2.skullking.application.ports.output.LoadGamePort
import com.tamj0rd2.skullking.domain.game.GameEvent

class SendGameNotificationsService(
    private val sendGameNotificationPort: SendGameNotificationPort,
    private val loadGamePort: LoadGamePort,
) : GameEventSubscriber {

    override fun notify(event: GameEvent) {
        val game = checkNotNull(loadGamePort.load(event.gameId))

        game.players.forEach {
            sendGameNotificationPort.send(
                it,
                PlayerSpecificGameState(gameId = game.id, players = game.players.toList()),
            )
        }
    }
}
