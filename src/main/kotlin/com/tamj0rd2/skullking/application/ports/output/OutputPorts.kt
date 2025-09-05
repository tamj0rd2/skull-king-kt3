package com.tamj0rd2.skullking.application.ports.output

import com.tamj0rd2.skullking.application.ports.SendGameNotificationPort
import com.tamj0rd2.skullking.application.ports.SubscribeToGameNotificationsPort

data class OutputPorts(
    val gameEventStore: GameEventStore,
    val subscribeToGameNotificationsPort: SubscribeToGameNotificationsPort,
    val sendGameNotificationPort: SendGameNotificationPort,
) {
    companion object
}
