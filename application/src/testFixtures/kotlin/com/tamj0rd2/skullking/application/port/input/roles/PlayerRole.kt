package com.tamj0rd2.skullking.application.port.input.roles

import com.tamj0rd2.skullking.application.ApplicationDriver
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import dev.forkhandles.values.ZERO

class PlayerRole(
    private val driver: ApplicationDriver,
) {
    var playerId = PlayerId.ZERO
        private set

    fun createsAGame(): GameId = driver(CreateNewGameCommand).gameId

    fun joinsAGame(gameId: GameId): PlayerId = driver(JoinGameCommand(gameId)).playerId.also { playerId = it }
}
