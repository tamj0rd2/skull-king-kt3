package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.GameNotification
import com.tamj0rd2.skullking.application.ports.SendGameNotificationPort
import com.tamj0rd2.skullking.application.ports.SubscribeToGameNotificationsPort
import com.tamj0rd2.skullking.application.ports.input.JoinGameInput
import com.tamj0rd2.skullking.application.ports.input.JoinGameOutput
import com.tamj0rd2.skullking.application.ports.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.ports.output.GameEventSubscriber
import com.tamj0rd2.skullking.application.ports.output.LoadGamePort
import com.tamj0rd2.skullking.application.ports.output.SaveGamePort
import com.tamj0rd2.skullking.domain.game.GameEvent

class JoinGameService(
    private val saveGamePort: SaveGamePort,
    private val loadGamePort: LoadGamePort,
    private val subscribeToGameNotificationsPort: SubscribeToGameNotificationsPort,
    private val sendGameNotificationPort: SendGameNotificationPort,
) : JoinGameUseCase, GameEventSubscriber {
    override fun execute(input: JoinGameInput): JoinGameOutput {
        val game = loadGamePort.load(input.gameId)!!.addPlayer(input.playerId)
        saveGamePort.save(game)
        subscribeToGameNotificationsPort.subscribe(input.receiveGameNotification)
        return JoinGameOutput
    }

    override fun notify(event: GameEvent) {
        sendGameNotificationPort.send(event.toGameNotification())
    }

    private fun GameEvent.toGameNotification(): GameNotification.PlayerJoined =
        when (this) {
            is GameEvent.PlayerJoined -> GameNotification.PlayerJoined(this.playerId)
        }
}
