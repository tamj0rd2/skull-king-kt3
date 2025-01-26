package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.output.FindPlayerIdPort
import com.tamj0rd2.skullking.application.port.output.LobbyNotifier
import com.tamj0rd2.skullking.application.port.output.LobbyRepository
import com.tamj0rd2.skullking.application.port.output.SavePlayerIdPort
import com.tamj0rd2.skullking.application.service.CreateNewLobbyService
import com.tamj0rd2.skullking.application.service.JoinALobbyService
import com.tamj0rd2.skullking.application.service.PlaceABidService
import com.tamj0rd2.skullking.application.service.PlayACardService
import com.tamj0rd2.skullking.application.service.StartGameService

class SkullKingApplication private constructor(
    private val createNewLobbyUseCase: CreateNewLobbyUseCase,
    private val joinALobbyUseCase: JoinALobbyUseCase,
    private val startGameUseCase: StartGameUseCase,
    private val placeABidUseCase: PlaceABidUseCase,
    private val playACardUseCase: PlayACardUseCase,
) : SkullKingUseCases,
    CreateNewLobbyUseCase by createNewLobbyUseCase,
    JoinALobbyUseCase by joinALobbyUseCase,
    PlaceABidUseCase by placeABidUseCase,
    StartGameUseCase by startGameUseCase,
    PlayACardUseCase by playACardUseCase {
    constructor(
        lobbyRepository: LobbyRepository,
        lobbyNotifier: LobbyNotifier,
        findPlayerIdPort: FindPlayerIdPort,
        savePlayerIdPort: SavePlayerIdPort,
    ) : this(
        createNewLobbyUseCase =
            CreateNewLobbyService(
                lobbyRepository = lobbyRepository,
                lobbyNotifier = lobbyNotifier,
                savePlayerIdPort = savePlayerIdPort,
            ),
        joinALobbyUseCase =
            JoinALobbyService(
                lobbyRepository = lobbyRepository,
                lobbyNotifier = lobbyNotifier,
                findPlayerIdPort = findPlayerIdPort,
                savePlayerIdPort = savePlayerIdPort,
            ),
        startGameUseCase = StartGameService(lobbyRepository, lobbyNotifier),
        placeABidUseCase =
            PlaceABidService(
                lobbyNotifier = lobbyNotifier,
                lobbyRepository = lobbyRepository,
            ),
        playACardUseCase =
            PlayACardService(
                lobbyNotifier = lobbyNotifier,
            ),
    )

    companion object
}
