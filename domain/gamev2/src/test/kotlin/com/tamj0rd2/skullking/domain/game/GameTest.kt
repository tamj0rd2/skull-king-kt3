package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameErrorCode.NotEnoughPlayersToStartGame
import dev.forkhandles.values.random
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.first
import strikt.assertions.isA

class GameTest {
    companion object {
        val atLeast2Players = listOf(PlayerId.random(), PlayerId.random())
    }

    @Nested
    inner class StartingAGame {
        @Test
        fun `a new game starts with a GameStarted event`() {
            val game = Game(atLeast2Players)
            expectThat(game.events).first().isA<GameEvent.GameStarted>()
        }

        @Test
        fun `cannot start a game without players`() {
            val players = emptyList<PlayerId>()
            expectThrows<NotEnoughPlayersToStartGame> { Game(players) }
        }

        @Test
        fun `cannot start a game with a single player`() {
            val players = listOf(PlayerId.random())
            expectThrows<NotEnoughPlayersToStartGame> { Game(players) }
        }
    }
}
