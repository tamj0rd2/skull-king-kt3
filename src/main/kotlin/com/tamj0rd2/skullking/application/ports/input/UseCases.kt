package com.tamj0rd2.skullking.application.ports.input

data class UseCases(
    val createGameUseCase: CreateGameUseCase,
    val viewGamesUseCase: ViewGamesUseCase,
    val joinGameUseCase: JoinGameUseCase,
    val startGameUseCase: StartGameUseCase,
) {
    companion object
}
