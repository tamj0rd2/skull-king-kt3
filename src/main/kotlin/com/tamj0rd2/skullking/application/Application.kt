package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.ports.input.CreateGameUseCase
import com.tamj0rd2.skullking.application.ports.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.ports.input.ViewGamesUseCase
import com.tamj0rd2.skullking.application.services.CreateGameService
import com.tamj0rd2.skullking.application.services.JoinGameService
import com.tamj0rd2.skullking.application.services.ViewGamesService

data class Application(
    val createGameUseCase: CreateGameUseCase,
    val viewGamesUseCase: ViewGamesUseCase,
    val joinGameUseCase: JoinGameUseCase,
) {
    companion object {
        fun create(outputPorts: OutputPorts) =
            Application(
                createGameUseCase = CreateGameService(outputPorts.saveGamePort),
                viewGamesUseCase = ViewGamesService(outputPorts.findGamesPort),
                joinGameUseCase =
                    JoinGameService(
                            saveGamePort = outputPorts.saveGamePort,
                            loadGamePort = outputPorts.loadGamePort,
                            subscribeToGameNotificationsPort =
                                outputPorts.subscribeToGameNotificationsPort,
                            sendGameNotificationPort = outputPorts.sendGameNotificationPort,
                        )
                        .also { outputPorts.subscribeToGameEventsPort.subscribe(it) },
            )
    }
}
