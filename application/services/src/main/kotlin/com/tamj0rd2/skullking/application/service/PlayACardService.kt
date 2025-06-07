package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardCommand
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardOutput
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.domain.game.LobbyCommand
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.orThrow

class PlayACardService(private val lobbyRepository: LobbyRepository) : PlayACardUseCase {
    override fun invoke(command: PlayACardCommand): Result4k<PlayACardOutput, LobbyErrorCode> {
        val lobby = lobbyRepository.load(command.lobbyId)
        lobby.execute(LobbyCommand.PlayACard(command.playerId, command.card)).orThrow()
        lobbyRepository.save(lobby)
        return PlayACardOutput.asSuccess()
    }
}
