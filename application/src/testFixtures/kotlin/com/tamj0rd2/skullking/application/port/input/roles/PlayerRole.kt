package com.tamj0rd2.skullking.application.port.input.roles

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.GameId
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import com.tamj0rd2.testhelpers.eventually
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.ZERO
import strikt.api.expectThat
import strikt.assertions.contains

class PlayerRole(
    private val driver: SkullKingUseCases,
) : GameUpdateListener {
    var id = PlayerId.ZERO
        private set

    private var gameId = GameId.NONE

    private val receivedGameUpdates = mutableListOf<GameUpdate>()

    fun createsAGame(): GameId = driver(CreateNewGameCommand).gameId

    fun joinsAGame(gameId: GameId): PlayerId {
        this.gameId = gameId
        val command = JoinGameCommand(gameId = gameId, gameUpdateListener = this)
        return driver(command).orThrow().playerId.also { id = it }
    }

    fun startsTheGame() {
        driver(StartGameCommand(gameId, id)).orThrow()
    }

    private var gameUpdatesRead = 0

    fun received(expectedUpdate: GameUpdate) {
        eventually {
            val recentGameUpdates = receivedGameUpdates.drop(gameUpdatesRead)
            expectThat(recentGameUpdates).contains(expectedUpdate)
            gameUpdatesRead = recentGameUpdates.indexOfFirst { it == expectedUpdate } + 1
        }
    }

    override fun send(updates: List<GameUpdate>) {
        receivedGameUpdates += updates
    }
}
