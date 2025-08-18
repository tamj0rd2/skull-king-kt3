package com.tamj0rd2.skullking

import com.tamj0rd2.skullking.adapters.inmemory.InMemoryGameNotifier
import com.tamj0rd2.skullking.adapters.inmemory.InMemoryGameRepository
import com.tamj0rd2.skullking.application.OutputPorts

fun createOutputPortsForTesting(): OutputPorts {
    val gameRepository = InMemoryGameRepository()
    val gameNotifier = InMemoryGameNotifier()

    return OutputPorts(
        saveGamePort = gameRepository,
        findGamesPort = gameRepository,
        loadGamePort = gameRepository,
        subscribeToGameNotificationsPort = gameNotifier,
        sendGameNotificationPort = gameNotifier,
        subscribeToGameEventsPort = gameRepository,
    )
}
