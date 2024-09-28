package com.tamj0rd2.skullking.application.port.input

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
}
