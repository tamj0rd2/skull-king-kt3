package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.GameUpdate
import com.tamj0rd2.skullking.domain.model.PlayerId
import dev.forkhandles.values.ZERO
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNotEqualTo

abstract class JoinGameGameUseCaseContract : GameUseCaseContract {
    @Test
    fun `can join a game`() {
        val player = newPlayerRole()
        val gameId = player.createsAGame()
        val playerId = player.joinsAGame(gameId)
        expectThat(playerId).isNotEqualTo(PlayerId.ZERO)
    }

    @Test
    fun `given there is already a player in a game, when another player joins, the first player is notified about it`() {
        val player1 = newPlayerRole()
        val player2 = newPlayerRole()

        val gameId = player1.createsAGame()
        player1.joinsAGame(gameId)
        player2.joinsAGame(gameId)

        player1.received(GameUpdate.PlayerJoined(player2.id))
    }
}

class InMemoryEventListeners {
    private val listeners = mutableListOf<GameUpdateListener>()

    fun register(listener: GameUpdateListener) {
        listeners.add(listener)
    }

    fun broadcast(newEvents: List<GameUpdate>) {
        listeners.forEach { it.notify(newEvents) }
    }
}
