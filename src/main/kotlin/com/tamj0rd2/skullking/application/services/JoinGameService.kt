package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.GameNotification
import com.tamj0rd2.skullking.application.ports.SendGameNotificationPort
import com.tamj0rd2.skullking.application.ports.SubscribeToGameNotificationsPort
import com.tamj0rd2.skullking.application.ports.input.JoinGameInput
import com.tamj0rd2.skullking.application.ports.input.JoinGameOutput
import com.tamj0rd2.skullking.application.ports.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.ports.output.LoadGamePort
import com.tamj0rd2.skullking.application.ports.output.SaveGamePort
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameEvent

class JoinGameService(
    private val saveGamePort: SaveGamePort,
    private val loadGamePort: LoadGamePort,
    private val subscribeToGameNotificationsPort: SubscribeToGameNotificationsPort,
    private val sendGameNotificationPort: SendGameNotificationPort,
) : JoinGameUseCase {
    override fun execute(input: JoinGameInput): JoinGameOutput {
        val game = loadGamePort.load(input.gameId)!!.addPlayer(input.playerId)
        saveGamePort.save(game)

        subscribeToGameNotificationsPort.subscribe(input.receiveGameNotification)
        getNotificationsForCatchupSubscription(game).forEach(input.receiveGameNotification::receive)

        sendGameNotificationPort.send(GameNotification.PlayerJoined(input.playerId))
        return JoinGameOutput
    }

    private fun getNotificationsForCatchupSubscription(game: Game): List<GameNotification> {
        return game.events.map { event ->
            when (event) {
                is GameEvent.PlayerJoined -> GameNotification.PlayerJoined(event.playerId)
            }
        }
    }
}
