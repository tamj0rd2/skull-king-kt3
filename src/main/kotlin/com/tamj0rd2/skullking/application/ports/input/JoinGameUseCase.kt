package com.tamj0rd2.skullking.application.ports.input

import com.tamj0rd2.skullking.domain.game.GameId

fun interface JoinGameUseCase : UseCase<JoinGameInput, JoinGameOutput>

data class JoinGameInput(val gameId: GameId)

data object JoinGameOutput
