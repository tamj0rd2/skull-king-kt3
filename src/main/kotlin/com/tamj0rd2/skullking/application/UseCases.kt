package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.ports.input.CreateGameUseCase
import com.tamj0rd2.skullking.application.ports.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.ports.input.UseCase
import com.tamj0rd2.skullking.application.ports.input.ViewGamesUseCase
import com.tamj0rd2.skullking.application.services.CreateGameService
import com.tamj0rd2.skullking.application.services.JoinGameService
import com.tamj0rd2.skullking.application.services.SendGameNotificationsService
import com.tamj0rd2.skullking.application.services.ViewGamesService

data class UseCases(
    val createGameUseCase: CreateGameUseCase,
    val viewGamesUseCase: ViewGamesUseCase,
    val joinGameUseCase: JoinGameUseCase,
) {
    companion object {
        fun createFrom(outputPorts: OutputPorts): UseCases {
            outputPorts.subscribeToGameEventsPort.subscribe(
                SendGameNotificationsService(
                    sendGameNotificationPort = outputPorts.sendGameNotificationPort,
                    loadGamePort = outputPorts.loadGamePort,
                )
            )

            return UseCases(
                createGameUseCase = CreateGameService(outputPorts.saveGamePort),
                viewGamesUseCase = ViewGamesService(outputPorts.findGamesPort),
                joinGameUseCase =
                    JoinGameService(
                        saveGamePort = outputPorts.saveGamePort,
                        loadGamePort = outputPorts.loadGamePort,
                        subscribeToGameNotificationsPort =
                            outputPorts.subscribeToGameNotificationsPort,
                    ),
            )
        }
    }

    fun monitorWith(
        inputMonitor: (input: Any) -> Unit = { _ -> },
        outputMonitor: (output: Any?) -> Unit = { _ -> },
    ): UseCases {
        fun <I : Any, O : Any> UseCase<I, O>.decorate(): (I) -> O = { input: I ->
            inputMonitor(input)
            execute(input).also { outputMonitor(it) }
        }

        return UseCases(
            createGameUseCase = createGameUseCase.decorate(),
            viewGamesUseCase = viewGamesUseCase.decorate(),
            joinGameUseCase = joinGameUseCase.decorate(),
        )
    }
}
