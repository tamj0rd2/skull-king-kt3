package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.service.CreateNewGameService
import com.tamj0rd2.skullking.application.service.JoinGameService
import com.tamj0rd2.skullking.application.service.ViewPlayerGameStateService

class ApplicationDomainDriver private constructor(
    private val createNewGameService: CreateNewGameService,
    private val joinGameService: JoinGameService,
    private val viewPlayerGameStateService: ViewPlayerGameStateService,
) : ApplicationDriver {
    constructor(
        gameRepository: GameRepository,
    ) : this(
        createNewGameService = CreateNewGameService(gameRepository),
        joinGameService = JoinGameService(gameRepository),
        viewPlayerGameStateService = ViewPlayerGameStateService(gameRepository),
    )

    override fun invoke(command: CreateNewGameCommand) = createNewGameService.invoke(command)

    override fun invoke(command: JoinGameCommand) = joinGameService.invoke(command)

    override fun invoke(query: ViewPlayerGameStateQuery) = viewPlayerGameStateService.invoke(query)

    companion object
}
