package com.tamj0rd2.skullking.adapters.inmemory

import com.tamj0rd2.skullking.application.OutputPorts

fun OutputPorts.Companion.inMemory(): OutputPorts {
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
