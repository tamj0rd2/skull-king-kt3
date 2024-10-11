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
import strikt.assertions.isEqualTo

class PlayerRole(
    private val driver: SkullKingUseCases,
) : GameUpdateListener {
    var id = PlayerId.ZERO
        private set

    private var gameId = GameId.NONE

    private val receivedEvents = mutableListOf<GameUpdate>()

    fun createsAGame(): GameId = driver(CreateNewGameCommand).gameId

    fun joinsAGame(gameId: GameId): PlayerId {
        this.gameId = gameId
        val command = JoinGameCommand(gameId = gameId, gameUpdateListener = this)
        return driver(command).orThrow().playerId.also { id = it }
    }

    fun startsTheGame() {
        driver(StartGameCommand(gameId, id)).orThrow()
    }

    fun received(expectedNotification: GameUpdate): Unit =
        eventually {
            // TODO: this assertion is obviously going to break very quickly.
            expectThat(receivedEvents.toList()).isEqualTo(listOf(expectedNotification))
        }

    override fun send(updates: List<GameUpdate>) {
        receivedEvents += updates
    }
}
