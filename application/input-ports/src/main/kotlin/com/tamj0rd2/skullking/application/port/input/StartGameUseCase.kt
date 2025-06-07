package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k

interface StartGameUseCase {
    operator fun invoke(command: StartGameCommand): Result4k<StartGameOutput, LobbyErrorCode>

    data class StartGameCommand(val lobbyId: LobbyId, val playerId: PlayerId)

    data object StartGameOutput
}
