package com.tamj0rd2.skullking.application.ports

import com.tamj0rd2.skullking.domain.game.PlayerId

interface GameNotifier : SubscribeToGameNotificationsPort, SendGameNotificationPort

fun interface SubscribeToGameNotificationsPort {
    fun subscribe(receiver: ReceiveGameNotification)
}

fun interface SendGameNotificationPort {
    fun send(gameNotification: GameNotification)
}

fun interface ReceiveGameNotification {
    fun receive(gameNotification: GameNotification)
}

sealed interface GameNotification {
    data class PlayerJoined(val playerId: PlayerId) : GameNotification
}
