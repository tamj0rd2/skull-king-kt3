package com.tamj0rd2.skullking.adapters.inmemory

import com.tamj0rd2.skullking.application.ports.output.OutputPorts

fun OutputPorts.Companion.inMemory(): OutputPorts {
    val gameNotifier = InMemoryGameNotifier()

    return OutputPorts(
        gameEventStore = InMemoryGameEventStore(),
        subscribeToGameNotificationsPort = gameNotifier,
        sendGameNotificationPort = gameNotifier,
    )
}
