package com.tamj0rd2.skullking.application.ports.output

import com.tamj0rd2.skullking.application.ports.SendGameNotificationPort
import com.tamj0rd2.skullking.application.ports.SubscribeToGameNotificationsPort

data class OutputPorts(
    val saveGamePort: SaveGamePort,
    val findGamesPort: FindGamesPort,
    val loadGamePort: LoadGamePort,
    val subscribeToGameNotificationsPort: SubscribeToGameNotificationsPort,
    val sendGameNotificationPort: SendGameNotificationPort,
    val subscribeToGameEventsPort: SubscribeToGameEventsPort,
) {
    companion object
}
