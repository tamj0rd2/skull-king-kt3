package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.application.service.CreateNewGameService
import com.tamj0rd2.skullking.application.service.JoinGameService
import com.tamj0rd2.skullking.application.service.StartGameService

class ApplicationDomainDriver private constructor(
    private val createNewGameService: CreateNewGameService,
    private val joinGameService: JoinGameService,
    private val startGameService: StartGameUseCase,
) : ApplicationDriver {
    constructor(
        gameRepository: GameRepository,
        gameUpdateNotifier: GameUpdateNotifier,
    ) : this(
        createNewGameService = CreateNewGameService(gameRepository),
        joinGameService = JoinGameService(gameRepository, gameUpdateNotifier),
        startGameService = StartGameService(gameUpdateNotifier),
    )

    override fun invoke(command: CreateNewGameCommand) = createNewGameService.invoke(command)

    override fun invoke(command: JoinGameCommand) = joinGameService.invoke(command)

    override fun invoke(command: StartGameCommand) = startGameService.invoke(command)

    companion object
}
