package com.tamj0rd2.skullking.application.port.input.roles

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.GameId
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import dev.forkhandles.values.ZERO
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Instant

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
        return driver(command).playerId.also { id = it }
    }

    fun startsTheGame() {
        val command = StartGameCommand(gameId, id)
        driver(command)
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

private fun <T> eventually(block: () -> T): T {
    val stopAt = Instant.now().plusMillis(200)
    var lastError: AssertionError?
    do {
        try {
            return block()
        } catch (e: AssertionError) {
            lastError = e
        }
    } while (stopAt > Instant.now())

    throw checkNotNull(lastError)
}
