package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCase
import com.tamj0rd2.skullking.application.port.input.MakeABidUseCase
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.output.FindPlayerIdPort
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.application.port.output.SavePlayerIdPort
import com.tamj0rd2.skullking.application.service.CreateNewGameService
import com.tamj0rd2.skullking.application.service.JoinAGameService
import com.tamj0rd2.skullking.application.service.MakeABidService
import com.tamj0rd2.skullking.application.service.StartGameService

class SkullKingApplication private constructor(
    private val createNewGameUseCase: CreateNewGameUseCase,
    private val joinAGameUseCase: JoinAGameUseCase,
    private val startGameUseCase: StartGameUseCase,
    private val makeABidUseCase: MakeABidUseCase,
) : SkullKingUseCases,
    CreateNewGameUseCase by createNewGameUseCase,
    JoinAGameUseCase by joinAGameUseCase,
    MakeABidUseCase by makeABidUseCase,
    StartGameUseCase by startGameUseCase {
    constructor(
        gameRepository: GameRepository,
        gameUpdateNotifier: GameUpdateNotifier,
        findPlayerIdPort: FindPlayerIdPort,
        savePlayerIdPort: SavePlayerIdPort,
    ) : this(
        createNewGameUseCase =
            CreateNewGameService(
                gameRepository = gameRepository,
                gameUpdateNotifier = gameUpdateNotifier,
                savePlayerIdPort = savePlayerIdPort,
            ),
        joinAGameUseCase =
            JoinAGameService(
                gameRepository = gameRepository,
                gameUpdateNotifier = gameUpdateNotifier,
                findPlayerIdPort = findPlayerIdPort,
                savePlayerIdPort = savePlayerIdPort,
            ),
        startGameUseCase = StartGameService(gameRepository, gameUpdateNotifier),
        makeABidUseCase =
            MakeABidService(
                gameUpdateNotifier = gameUpdateNotifier,
                gameRepository = gameRepository,
            ),
    )

    companion object
}
