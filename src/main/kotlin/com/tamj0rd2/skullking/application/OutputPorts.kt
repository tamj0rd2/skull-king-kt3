package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.ports.SendGameNotificationPort
import com.tamj0rd2.skullking.application.ports.SubscribeToGameNotificationsPort
import com.tamj0rd2.skullking.application.ports.output.FindGamesPort
import com.tamj0rd2.skullking.application.ports.output.LoadGamePort
import com.tamj0rd2.skullking.application.ports.output.SaveGamePort
import com.tamj0rd2.skullking.application.ports.output.SubscribeToGameEventsPort

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
