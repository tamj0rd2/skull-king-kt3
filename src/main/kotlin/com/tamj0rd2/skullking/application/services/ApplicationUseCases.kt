package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.input.UseCases
import com.tamj0rd2.skullking.application.ports.output.OutputPorts

fun UseCases.Companion.using(outputPorts: OutputPorts): UseCases {
    outputPorts.subscribeToGameEventsPort.subscribe(
        SendGameNotificationsService(
            sendGameNotificationPort = outputPorts.sendGameNotificationPort,
            loadGamePort = outputPorts.loadGamePort,
        )
    )

    return UseCases(
        createGameUseCase =
            CreateGameService(
                saveGamePort = outputPorts.saveGamePort,
                subscribeToGameNotificationsPort = outputPorts.subscribeToGameNotificationsPort,
            ),
        viewGamesUseCase = ViewGamesService(outputPorts.findGamesPort),
        joinGameUseCase =
            JoinGameService(
                saveGamePort = outputPorts.saveGamePort,
                loadGamePort = outputPorts.loadGamePort,
                subscribeToGameNotificationsPort = outputPorts.subscribeToGameNotificationsPort,
            ),
    )
}
