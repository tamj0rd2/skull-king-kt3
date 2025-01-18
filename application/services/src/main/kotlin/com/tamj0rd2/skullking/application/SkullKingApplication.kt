package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCase
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.output.FindPlayerIdPort
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.application.port.output.SavePlayerIdPort
import com.tamj0rd2.skullking.application.service.CreateNewGameService
import com.tamj0rd2.skullking.application.service.JoinAGameService
import com.tamj0rd2.skullking.application.service.PlaceABidService
import com.tamj0rd2.skullking.application.service.PlayACardService
import com.tamj0rd2.skullking.application.service.StartGameService

class SkullKingApplication private constructor(
    private val createNewGameUseCase: CreateNewGameUseCase,
    private val joinAGameUseCase: JoinAGameUseCase,
    private val startGameUseCase: StartGameUseCase,
    private val placeABidUseCase: PlaceABidUseCase,
    private val playACardUseCase: PlayACardUseCase,
) : SkullKingUseCases,
    CreateNewGameUseCase by createNewGameUseCase,
    JoinAGameUseCase by joinAGameUseCase,
    PlaceABidUseCase by placeABidUseCase,
    StartGameUseCase by startGameUseCase,
    PlayACardUseCase by playACardUseCase {
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
        placeABidUseCase =
            PlaceABidService(
                gameUpdateNotifier = gameUpdateNotifier,
                gameRepository = gameRepository,
            ),
        playACardUseCase =
            PlayACardService(
                gameUpdateNotifier = gameUpdateNotifier,
            ),
    )

    companion object
}
