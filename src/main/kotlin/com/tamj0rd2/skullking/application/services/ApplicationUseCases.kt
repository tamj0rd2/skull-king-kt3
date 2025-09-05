package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.input.UseCases
import com.tamj0rd2.skullking.application.ports.output.OutputPorts
import com.tamj0rd2.skullking.application.repositories.GameRepository

fun UseCases.Companion.using(outputPorts: OutputPorts): UseCases {
    val gameRepository = GameRepository(outputPorts.gameEventStore)

    gameRepository.subscribe(
        SendGameNotificationsService(sendGameNotificationPort = outputPorts.sendGameNotificationPort, gameRepository = gameRepository)
    )

    return UseCases(
        createGameUseCase =
            CreateGameService(
                gameRepository = gameRepository,
                subscribeToGameNotificationsPort = outputPorts.subscribeToGameNotificationsPort,
            ),
        viewGamesUseCase = ViewGamesService(gameRepository),
        joinGameUseCase =
            JoinGameService(
                gameRepository = gameRepository,
                subscribeToGameNotificationsPort = outputPorts.subscribeToGameNotificationsPort,
            ),
        startGameUseCase = StartGameService(gameRepository = gameRepository),
    )
}
