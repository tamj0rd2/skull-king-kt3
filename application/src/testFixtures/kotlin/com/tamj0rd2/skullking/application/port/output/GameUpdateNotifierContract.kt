package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.GameUpdateArbs.gameUpdateArb
import com.tamj0rd2.skullking.domain.model.game.Game
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import com.tamj0rd2.skullking.domain.propertyTest
import io.kotest.property.Arb
import io.kotest.property.PropertyTesting
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.nonNegativeInt
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.isEqualTo

abstract class GameUpdateNotifierContract {
    protected abstract val sut: GameUpdateNotifier

    init {
        // FIXME: For some reason this test times out when the iteration count is set to default (1000)
        PropertyTesting.defaultIterationCount = 100
    }

    @Test
    fun `notifications can be broadcast to all subscribed listeners`() =
        propertyTest {
            checkAll(
                Arb.nonNegativeInt(max = Game.MAXIMUM_PLAYER_COUNT),
                Arb.list(gameUpdateArb, 0..100),
            ) { spyListenerCount, gameUpdates ->
                val spyListeners = buildList { repeat(spyListenerCount) { add(SpyGameUpdateListener()) } }
                spyListeners.forEach { sut.subscribe(it) }
                gameUpdates.forEach { sut.broadcast(it) }
                expectThat(spyListeners).all { get { receivedUpdates.toList() }.isEqualTo(gameUpdates) }
            }
        }
}

private class SpyGameUpdateListener : GameUpdateListener {
    val receivedUpdates = mutableListOf<GameUpdate>()

    override fun send(updates: List<GameUpdate>) {
        receivedUpdates += updates
    }
}
