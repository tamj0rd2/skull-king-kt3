package com.tamj0rd2.skullking.application.ports

import com.tamj0rd2.skullking.domain.game.PlayerId

fun interface ReceiveGameNotification {
    fun receive(gameNotification: GameNotification)
}

sealed interface GameNotification {
    data class PlayerJoined(val playerId: PlayerId) : GameNotification
}
