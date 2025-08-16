package com.tamj0rd2.skullking.application.ports.input

import com.tamj0rd2.skullking.application.ports.ReceiveGameNotification
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId

fun interface JoinGameUseCase : UseCase<JoinGameInput, JoinGameOutput>

data class JoinGameInput(
    val gameId: GameId,
    val receiveGameNotification: ReceiveGameNotification,
    val playerId: PlayerId,
)

data object JoinGameOutput
