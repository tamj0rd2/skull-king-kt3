package com.tamj0rd2.skullking.application.ports.input

import com.tamj0rd2.skullking.domain.game.GameId

fun interface CreateGameUseCase : UseCase<CreateGameInput, CreateGameOutput>

data object CreateGameInput

data class CreateGameOutput(val gameId: GameId)
