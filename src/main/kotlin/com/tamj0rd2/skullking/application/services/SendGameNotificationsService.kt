package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import com.tamj0rd2.skullking.application.ports.SendGameNotificationPort
import com.tamj0rd2.skullking.application.ports.output.GameEventSubscriber
import com.tamj0rd2.skullking.application.repositories.GameRepository
import com.tamj0rd2.skullking.domain.game.GameEvent

class SendGameNotificationsService(
    private val sendGameNotificationPort: SendGameNotificationPort,
    private val gameRepository: GameRepository,
) : GameEventSubscriber {

    override fun notify(event: GameEvent) {
        val (game, _) = gameRepository.load(event.gameId)

        println(game)
        game.players.forEach { playerId ->
            sendGameNotificationPort.send(
                playerId,
                PlayerSpecificGameState(
                    gameId = game.id,
                    players = game.players.toList(),
                    roundNumber = game.roundNumber,
                    myBid = game.placedBids[playerId],
                    phase = game.phase,
                ),
            )
        }
    }
}
