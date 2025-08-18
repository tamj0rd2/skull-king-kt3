package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.GameNotification
import com.tamj0rd2.skullking.application.ports.SendGameNotificationPort
import com.tamj0rd2.skullking.application.ports.output.GameEventSubscriber
import com.tamj0rd2.skullking.domain.game.GameEvent

class SendGameNotificationsService(private val sendGameNotificationPort: SendGameNotificationPort) :
    GameEventSubscriber {

    override fun notify(event: GameEvent) {
        sendGameNotificationPort.send(event.toGameNotification())
    }

    private fun GameEvent.toGameNotification(): GameNotification.PlayerJoined =
        when (this) {
            is GameEvent.PlayerJoined -> GameNotification.PlayerJoined(this.playerId)
        }
}
