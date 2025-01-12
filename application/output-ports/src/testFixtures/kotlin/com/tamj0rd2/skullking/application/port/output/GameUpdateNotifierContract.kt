package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.application.port.inandout.GameUpdate
import com.tamj0rd2.skullking.application.port.inandout.GameUpdateArbs
import com.tamj0rd2.skullking.application.port.inandout.GameUpdateListener
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameArbs
import com.tamj0rd2.skullking.domain.game.listOfSize
import com.tamj0rd2.skullking.domain.game.propertyTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.nonNegativeInt
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.isEqualTo

abstract class GameUpdateNotifierContract {
    protected abstract val sut: GameUpdateNotifier

    @Test
    fun `notifications can be broadcast to all listeners subscribed to a specific game`() =
        propertyTest {
            checkAll(
                Arb.nonNegativeInt(max = Game.MAXIMUM_PLAYER_COUNT),
                Arb.list(GameUpdateArbs.gameUpdateArb),
                Arb.list(GameUpdateArbs.gameUpdateArb),
                GameArbs.gameIdArb,
                GameArbs.gameIdArb,
            ) { listenerCount, gameUpdates, gameUpdateForAnotherGame, thisGameId, anotherGameId ->
                val spyListenersForThisGame =
                    listOfSize(listenerCount, ::SpyGameUpdateListener).onEach { sut.subscribe(thisGameId, it) }
                gameUpdates.forEach { sut.broadcast(thisGameId, it) }
                gameUpdateForAnotherGame.forEach { sut.broadcast(anotherGameId, it) }
                expectThat(spyListenersForThisGame).all { get { receivedUpdates }.isEqualTo(gameUpdates) }
            }
        }

    @Test
    fun `when a listener subscribes late, they receive all updates already broadcast`() =
        propertyTest {
            checkAll(
                Arb.int(min = 1, max = Game.MAXIMUM_PLAYER_COUNT),
                Arb.list(GameUpdateArbs.gameUpdateArb),
                GameArbs.gameIdArb,
            ) { listenerCount, gameUpdates, gameId ->
                val alreadySubscribedListeners =
                    listOfSize(listenerCount, ::SpyGameUpdateListener).onEach { sut.subscribe(gameId, it) }
                gameUpdates.forEach { sut.broadcast(gameId, it) }
                expectThat(alreadySubscribedListeners).all { get { receivedUpdates }.isEqualTo(gameUpdates) }

                val lateSubscriber = SpyGameUpdateListener()
                sut.subscribe(gameId, lateSubscriber)
                expectThat(lateSubscriber).get { receivedUpdates }.isEqualTo(gameUpdates)
            }
        }

    private class SpyGameUpdateListener : GameUpdateListener {
        private val _receivedUpdates = mutableListOf<GameUpdate>()
        val receivedUpdates get() = _receivedUpdates.toList()

        override fun receive(updates: List<GameUpdate>) {
            _receivedUpdates += updates
        }
    }
}
