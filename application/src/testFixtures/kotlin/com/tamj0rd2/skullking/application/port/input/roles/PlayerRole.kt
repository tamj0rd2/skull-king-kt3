package com.tamj0rd2.skullking.application.port.input.roles

import com.tamj0rd2.skullking.application.ApplicationDriver
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.GameUpdate
import com.tamj0rd2.skullking.domain.model.PlayerId
import dev.forkhandles.values.ZERO
import strikt.api.expectThat
import strikt.assertions.contains

class PlayerRole(
    private val driver: ApplicationDriver,
) : GameUpdateListener {
    var id = PlayerId.ZERO
        private set

    private val receivedEvents = mutableListOf<GameUpdate>()

    fun createsAGame(): GameId = driver(CreateNewGameCommand).gameId

    fun joinsAGame(gameId: GameId): PlayerId = driver(JoinGameCommand(gameId)).playerId.also { id = it }

    fun received(expectedNotification: GameUpdate) {
        // TODO: this can break really easily. What if the same event is in the list multiple times?
        expectThat(receivedEvents).contains(expectedNotification)
    }

    override fun notify(updates: List<GameUpdate>) {
        receivedEvents += updates
    }
}
