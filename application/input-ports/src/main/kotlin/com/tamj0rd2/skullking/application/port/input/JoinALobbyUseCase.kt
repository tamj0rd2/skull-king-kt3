package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k

interface JoinALobbyUseCase {
    operator fun invoke(command: JoinALobbyCommand): Result4k<JoinALobbyOutput, LobbyErrorCode>

    data class JoinALobbyCommand(
        val sessionId: SessionId,
        val lobbyId: LobbyId,
        val lobbyNotificationListener: LobbyNotificationListener,
    )

    data class JoinALobbyOutput(
        val playerId: PlayerId,
    )
}
