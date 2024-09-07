package com.tamj0rd2.skullking

import com.tamj0rd2.skullking.domain.service.JoinGameService
import com.tamj0rd2.skullking.domain.service.ViewPlayerGameStateService
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateOutput
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import com.tamj0rd2.skullking.port.output.GameEventsPort

class ApplicationDomainDriver(
    private val gameEventsPort: GameEventsPort,
) : ApplicationDriver {
    override fun invoke(command: JoinGameCommand): JoinGameOutput = JoinGameService(gameEventsPort).invoke(command)

    override fun invoke(query: ViewPlayerGameStateQuery): ViewPlayerGameStateOutput =
        ViewPlayerGameStateService(gameEventsPort).invoke(query)
}
