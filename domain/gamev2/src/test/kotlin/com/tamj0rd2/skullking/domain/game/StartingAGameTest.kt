package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.game.GameEvent.GameStarted
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class StartingAGameTest {
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
}
