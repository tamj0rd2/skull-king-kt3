package com.tamj0rd2.skullking.application.ports

import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.domain.game.RoundNumber

interface GameNotifier : SubscribeToGameNotificationsPort, SendGameNotificationPort

fun interface SubscribeToGameNotificationsPort {
    fun subscribe(playerId: PlayerId, receiver: ReceiveGameNotification)
}

fun interface SendGameNotificationPort {
    fun send(recipient: PlayerId, playerSpecificGameState: PlayerSpecificGameState)
}

fun interface ReceiveGameNotification {
    fun receive(state: PlayerSpecificGameState)
}

data class PlayerSpecificGameState(val gameId: GameId, val players: List<PlayerId>, val roundNumber: RoundNumber?, val myBid: Bid?)
