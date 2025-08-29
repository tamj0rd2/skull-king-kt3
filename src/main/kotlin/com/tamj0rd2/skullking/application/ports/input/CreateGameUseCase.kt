package com.tamj0rd2.skullking.application.ports.input

import com.tamj0rd2.skullking.application.ports.ReceiveGameNotification
import com.tamj0rd2.skullking.domain.game.PlayerId

fun interface CreateGameUseCase : UseCase<CreateGameInput, CreateGameOutput>

data class CreateGameInput(
    val playerId: PlayerId,
    val receiveGameNotification: ReceiveGameNotification,
)

data object CreateGameOutput
