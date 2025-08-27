package com.tamj0rd2.skullking.application.ports

import com.tamj0rd2.skullking.domain.game.PlayerId

interface GameNotifier : SubscribeToGameNotificationsPort, SendGameNotificationPort

fun interface SubscribeToGameNotificationsPort {
    fun subscribe(playerId: PlayerId, receiver: ReceiveGameNotification)
}

fun interface SendGameNotificationPort {
    fun send(recipient: PlayerId, playerSpecificGameState: PlayerSpecificGameState)
}

fun interface ReceiveGameNotification {
    fun receive(playerSpecificGameState: PlayerSpecificGameState)
}

data class PlayerSpecificGameState(val players: List<PlayerId>)
