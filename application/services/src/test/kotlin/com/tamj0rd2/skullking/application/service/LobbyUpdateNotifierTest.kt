package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.domain.game.Card
import com.tamj0rd2.skullking.domain.game.Lobby
import com.tamj0rd2.skullking.domain.game.LobbyArbs
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.LobbyNotification
import com.tamj0rd2.skullking.domain.game.LobbyNotification.CardsWereDealt
import com.tamj0rd2.skullking.domain.game.LobbyNotification.TheGameHasStarted
import com.tamj0rd2.skullking.domain.game.LobbyNotificationArbs
import com.tamj0rd2.skullking.domain.game.LobbyNotificationRecipient.Someone
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.domain.game.listOfSize
import com.tamj0rd2.skullking.domain.game.propertyTest
import dev.forkhandles.values.random
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
                        .onEach { sut.subscribe(thisLobbyId, PlayerId.random(), it) }

                lobbyNotifications.forEach { sut.broadcast(thisLobbyId, listOf(it)) }
                gameUpdateForAnotherGame.forEach { sut.broadcast(anotherLobbyId, listOf(it)) }
                expectThat(spyListenersForThisGame).all { get { receivedUpdates }.isEqualTo(lobbyNotifications) }
            }
        }

    @Test
    fun `subscribers do not receive messages that weren't intended for them`() {
        val intendedPlayer = PlayerId.random()
        val playerSpecificNotification = CardsWereDealt(listOf(Card, Card), recipient = Someone(intendedPlayer))
        val nonPlayerSpecificNotification = TheGameHasStarted
        val lobbyId = LobbyId.random()

        val intendedSubscriber = SpyLobbyNotificationListener()
        val unintendedSubscriber = SpyLobbyNotificationListener()
        sut.subscribe(lobbyId, intendedPlayer, intendedSubscriber)
        sut.subscribe(lobbyId, PlayerId.random(), unintendedSubscriber)

        sut.broadcast(lobbyId, listOf(playerSpecificNotification, nonPlayerSpecificNotification))

        expectThat(intendedSubscriber).get { receivedUpdates }.isEqualTo(listOf(playerSpecificNotification, nonPlayerSpecificNotification))
        expectThat(unintendedSubscriber).get { receivedUpdates }.isEqualTo(listOf(nonPlayerSpecificNotification))
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
                        .onEach { sut.subscribe(lobbyId, PlayerId.random(), it) }

                lobbyNotifications.forEach { sut.broadcast(lobbyId, listOf(it)) }
                expectThat(alreadySubscribedListeners).all { get { receivedUpdates }.isEqualTo(lobbyNotifications) }

                val lateSubscriber = SpyLobbyNotificationListener()
                sut.subscribe(lobbyId, PlayerId.random(), lateSubscriber)
                expectThat(lateSubscriber).get { receivedUpdates }.isEqualTo(lobbyNotifications)
            }
        }

    @Test
    fun `when a listener subscribes late, they receive all updates already broadcast, except notifications sent to specific players`() {
        val playerSpecificNotification = CardsWereDealt(listOf(Card, Card), recipient = Someone(PlayerId.random()))
        val nonPlayerSpecificNotification = TheGameHasStarted
        val lobbyId = LobbyId.random()

        sut.broadcast(lobbyId, listOf(playerSpecificNotification, nonPlayerSpecificNotification))

        val lateSubscriber = SpyLobbyNotificationListener()
        sut.subscribe(lobbyId, PlayerId.random(), lateSubscriber)
        expectThat(lateSubscriber).get { receivedUpdates }.isEqualTo(listOf(nonPlayerSpecificNotification))
    }

    private class SpyLobbyNotificationListener : LobbyNotificationListener {
        private val _receivedUpdates = mutableListOf<LobbyNotification>()
        val receivedUpdates get() = _receivedUpdates.toList()

        override fun receive(updates: List<LobbyNotification>) {
            _receivedUpdates += updates
        }
    }
}
