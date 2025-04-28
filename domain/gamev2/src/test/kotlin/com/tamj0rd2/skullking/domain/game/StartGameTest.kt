package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.game.GameEvent.GameStarted
import com.tamj0rd2.skullking.domain.game.GamePhase.AwaitingNextRound
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class StartGameTest {
    @Test
    fun `a game always starts with a GameStarted event`() =
        propertyTest {
            checkAll(Arb.game) { game ->
                assert(game.events.first() is GameStarted)
            }
        }

    @Test
    fun `the GameStarted event only ever appears once`() =
        propertyTest {
            checkAll(Arb.game) { game ->
                val gameStartedEvents = game.events.filterIsInstance<GameStarted>()
                assert(gameStartedEvents.size == 1)
            }
        }

    @Test
    fun `the phase of new games is always AwaitingNextRound`() =
        propertyTest {
            checkAll(Arb.newGame) { game ->
                assert(game.state.phase == AwaitingNextRound)
            }
        }
}
