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
import strikt.assertions.isEqualTo
import java.time.Instant

class PlayerRole(
    private val driver: ApplicationDriver,
) : GameUpdateListener {
    var id = PlayerId.ZERO
        private set

    private val receivedEvents = mutableListOf<GameUpdate>()

    fun createsAGame(): GameId = driver(CreateNewGameCommand).gameId

    fun joinsAGame(gameId: GameId): PlayerId {
        val command = JoinGameCommand(gameId = gameId, gameUpdateListener = this)
        return driver(command).playerId.also { id = it }
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
