package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.domain.game.Lobby
import com.tamj0rd2.skullking.domain.game.LobbyArbs
import com.tamj0rd2.skullking.domain.game.LobbyNotification
import com.tamj0rd2.skullking.domain.game.LobbyNotificationArbs
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

class LobbyUpdateNotifierTest {
    private val sut: LobbyNotifier = LobbyNotifier()

    @Test
    fun `notifications can be broadcast to all listeners subscribed to a specific lobby`() =
        propertyTest {
            checkAll(
                Arb.nonNegativeInt(max = Lobby.MAXIMUM_PLAYER_COUNT),
                Arb.list(LobbyNotificationArbs.lobbyNotificationArb),
                Arb.list(LobbyNotificationArbs.lobbyNotificationArb),
                LobbyArbs.lobbyIdArb,
                LobbyArbs.lobbyIdArb,
            ) { listenerCount, lobbyNotifications, gameUpdateForAnotherGame, thisLobbyId, anotherLobbyId ->
                val spyListenersForThisGame =
                    listOfSize(listenerCount, ::SpyLobbyNotificationListener)
                        .onEach { sut.subscribe(thisLobbyId, it) }

                lobbyNotifications.forEach { sut.broadcast(thisLobbyId, listOf(it)) }
                gameUpdateForAnotherGame.forEach { sut.broadcast(anotherLobbyId, listOf(it)) }
                expectThat(spyListenersForThisGame).all { get { receivedUpdates }.isEqualTo(lobbyNotifications) }
            }
        }

    @Test
    fun `when a listener subscribes late, they receive all updates already broadcast`() =
        propertyTest {
            checkAll(
                Arb.int(min = 1, max = Lobby.MAXIMUM_PLAYER_COUNT),
                Arb.list(LobbyNotificationArbs.lobbyNotificationArb),
                LobbyArbs.lobbyIdArb,
            ) { listenerCount, lobbyNotifications, lobbyId ->
                val alreadySubscribedListeners =
                    listOfSize(listenerCount, ::SpyLobbyNotificationListener)
                        .onEach { sut.subscribe(lobbyId, it) }

                lobbyNotifications.forEach { sut.broadcast(lobbyId, listOf(it)) }
                expectThat(alreadySubscribedListeners).all { get { receivedUpdates }.isEqualTo(lobbyNotifications) }

                val lateSubscriber = SpyLobbyNotificationListener()
                sut.subscribe(lobbyId, lateSubscriber)
                expectThat(lateSubscriber).get { receivedUpdates }.isEqualTo(lobbyNotifications)
            }
        }

    private class SpyLobbyNotificationListener : LobbyNotificationListener {
        private val _receivedUpdates = mutableListOf<LobbyNotification>()
        val receivedUpdates get() = _receivedUpdates.toList()

        override fun receive(updates: List<LobbyNotification>) {
            _receivedUpdates += updates
        }
    }
}
