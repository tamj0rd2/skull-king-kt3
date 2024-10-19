package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.output.FindPlayerIdPort
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.application.port.output.SavePlayerIdPort
import com.tamj0rd2.skullking.application.service.CreateNewGameService
import com.tamj0rd2.skullking.application.service.JoinGameService
import com.tamj0rd2.skullking.application.service.StartGameService

class SkullKingApplication private constructor(
    private val createNewGameUseCase: CreateNewGameUseCase,
    private val joinGameUseCase: JoinGameUseCase,
    private val startGameUseCase: StartGameUseCase,
) : SkullKingUseCases,
    CreateNewGameUseCase by createNewGameUseCase,
    JoinGameUseCase by joinGameUseCase,
    StartGameUseCase by startGameUseCase {
    constructor(
        gameRepository: GameRepository,
        gameUpdateNotifier: GameUpdateNotifier,
        findPlayerIdPort: FindPlayerIdPort,
        savePlayerIdPort: SavePlayerIdPort,
    ) : this(
        createNewGameUseCase = CreateNewGameService(gameRepository),
        joinGameUseCase =
            JoinGameService(
                gameRepository = gameRepository,
                gameUpdateNotifier = gameUpdateNotifier,
                findPlayerIdPort = findPlayerIdPort,
                savePlayerIdPort = savePlayerIdPort,
            ),
        startGameUseCase = StartGameService(gameRepository, gameUpdateNotifier),
    )

    companion object
}
