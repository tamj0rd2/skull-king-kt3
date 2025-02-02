package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase.PlaceABidCommand
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase.PlaceABidOutput
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.domain.game.LobbyCommand
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.orThrow

class PlaceABidService(
    private val lobbyRepository: LobbyRepository,
) : PlaceABidUseCase {
    override fun invoke(command: PlaceABidCommand): Result4k<PlaceABidOutput, LobbyErrorCode> {
        val game = lobbyRepository.load(command.lobbyId)
        game.execute(LobbyCommand.PlaceBid(command.playerId, command.bid)).orThrow()
        lobbyRepository.save(game)
        return PlaceABidOutput.asSuccess()
    }
}
