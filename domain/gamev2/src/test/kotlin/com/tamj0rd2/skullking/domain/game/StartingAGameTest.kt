package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameEvent.GameStarted
import com.tamj0rd2.skullking.domain.game.PropertyTesting.gameInvariant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class StartingAGameTest {
    @Test
    fun `a game always starts with a GameStarted event`() =
        gameInvariant { game ->
            assert(game.events.first() is GameStarted)
        }

    @Test
    fun `the GameStarted event only ever appears once`() =
        gameInvariant { game ->
            val gameStartedEvents = game.events.filterIsInstance<GameStarted>()
            assert(gameStartedEvents.size == 1)
        }
}
