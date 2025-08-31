package com.tamj0rd2.skullking.application.ports.input

import com.tamj0rd2.skullking.domain.game.GameId

fun interface StartGameUseCase : UseCase<StartGameInput, StartGameOutput>

data class StartGameInput(val gameId: GameId)

data object StartGameOutput
