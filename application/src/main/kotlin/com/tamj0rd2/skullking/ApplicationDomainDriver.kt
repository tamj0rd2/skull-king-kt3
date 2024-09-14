package com.tamj0rd2.skullking

import com.tamj0rd2.skullking.domain.service.JoinGameService
import com.tamj0rd2.skullking.domain.service.ViewPlayerGameStateService
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import com.tamj0rd2.skullking.port.output.GameRepository

class ApplicationDomainDriver private constructor(
    private val joinGameService: JoinGameService,
    private val viewPlayerGameStateService: ViewPlayerGameStateService,
) : ApplicationDriver {
    override fun invoke(command: JoinGameCommand) = joinGameService.invoke(command)

    override fun invoke(query: ViewPlayerGameStateQuery) = viewPlayerGameStateService.invoke(query)

    companion object {
        fun create(gameRepository: GameRepository): ApplicationDomainDriver =
            ApplicationDomainDriver(
                joinGameService = JoinGameService(gameRepository),
                viewPlayerGameStateService = ViewPlayerGameStateService(gameRepository),
            )
    }
}
