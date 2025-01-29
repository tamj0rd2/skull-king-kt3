package com.tamj0rd2.skullking.application.port.input.testsupport

import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase.CreateNewLobbyCommand
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase.JoinALobbyCommand
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase.PlaceABidCommand
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardCommand
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.input.testsupport.PlayerRole.PlayerLobbyState.Companion.bids
import com.tamj0rd2.skullking.application.port.input.testsupport.PlayerRole.PlayerLobbyState.Companion.cardsInTrick
import com.tamj0rd2.skullking.application.port.input.testsupport.PlayerRole.PlayerLobbyState.Companion.players
import com.tamj0rd2.skullking.application.port.input.testsupport.PlayerRole.PlayerLobbyState.Companion.trickWinner
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.Card
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.LobbyNotification
import com.tamj0rd2.skullking.domain.game.LobbyNotification.ABidWasPlaced
import com.tamj0rd2.skullking.domain.game.LobbyNotification.ACardWasDealt
import com.tamj0rd2.skullking.domain.game.LobbyNotification.ACardWasPlayed
import com.tamj0rd2.skullking.domain.game.LobbyNotification.APlayerHasJoined
import com.tamj0rd2.skullking.domain.game.LobbyNotification.AllBidsHaveBeenPlaced
import com.tamj0rd2.skullking.domain.game.LobbyNotification.TheGameHasStarted
import com.tamj0rd2.skullking.domain.game.LobbyNotification.TheTrickHasEnded
import com.tamj0rd2.skullking.domain.game.PlayedCard
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.domain.game.RoundNumber
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
import strikt.api.Assertion.Builder
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.contains
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasEntry
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.one
import java.time.Instant

class PlayerRole(
    private val driver: SkullKingUseCases,
) : LobbyNotificationListener {
    var id = PlayerId.NONE
        private set
        get() {
            expectThat(field).isNotEqualTo(PlayerId.NONE)
            return field
        }

    // NOTE: for now I'm assuming the client generates the sessionId. I can have the server do this instead. The user can make an HTTP request
    // to create a session, which is returned to the user. Then the user can send that along with the request to connect to the websocket.
    // https://devcenter.heroku.com/articles/websocket-security#authentication-authorization
    // Essentially, in application terms, I need some kind of "command" to create and return a sessionId, before doing anything else.
    private val sessionId = SessionId.random()

    override fun toString(): String = id.toString()

    private var lobbyId = LobbyId.NONE
        get() {
            expectThat(field).isNotEqualTo(LobbyId.NONE)
            return field
        }

    private val receivedLobbyNotifications = mutableListOf<LobbyNotification>()

    fun `has created a lobby`() = `creates a lobby`()

    fun `creates a lobby`(): LobbyId {
        val output =
            driver(
                CreateNewLobbyCommand(
                    sessionId = sessionId,
                    lobbyNotificationListener = this,
                ),
            )

        expectThat(output.playerId).isNotEqualTo(PlayerId.NONE)
        id = output.playerId

        expectThat(output.lobbyId).isNotEqualTo(LobbyId.NONE)
        this.lobbyId = output.lobbyId
        return output.lobbyId
    }

    fun `accepts the lobby invite`() = `accept the lobby invite`()

    fun `accept the lobby invite`() {
        expectThat(lobbyId).isNotEqualTo(LobbyId.NONE)

        val command =
            JoinALobbyCommand(
                sessionId = sessionId,
                lobbyId = lobbyId,
                lobbyNotificationListener = this,
            )

        driver.invoke(command).orThrow().playerId.also {
            expectThat(it).isNotEqualTo(PlayerId.NONE)
            id = it
        }
    }

    fun `join the lobby again`() {
        expectThat(lobbyId).isNotEqualTo(LobbyId.NONE)
        `accept the lobby invite`()
    }

    private var latestErrorCode: LobbyErrorCode? = null

    fun triesTo(block: PlayerRole.() -> Unit) {
        expectThat(latestErrorCode).isNull()

        try {
            @Suppress("UNUSED_EXPRESSION")
            block()
        } catch (e: LobbyErrorCode) {
            latestErrorCode = e
        }
    }

    fun `gets the error`(expectedErrorCode: LobbyErrorCode) {
        expectThat(latestErrorCode).isNotNull().isA(expectedErrorCode::class.java)
        latestErrorCode = null
    }

    fun `starts the game`() {
        driver(StartGameCommand(lobbyId, id)).orThrow()
    }

    private var state = PlayerLobbyState()

    fun `sees them self in the lobby`() {
        hasLobbyStateWhere { players.contains(id) }
    }

    fun `sees each invited player in the lobby`() {
        expectThat(invitedPlayers.isNotEmpty())
        `sees exact players in the lobby`(this + invitedPlayers)
    }

    fun `sees exact players in the lobby`(expected: List<PlayerRole>) {
        hasLobbyStateWhere {
            players.hasSize(expected.size)
            players.containsExactlyInAnyOrder(expected.map { it.id })
            players.all { isNotEqualTo(PlayerId.NONE) }
        }
    }

    fun hasLobbyStateWhere(assertion: Builder<PlayerLobbyState>.() -> Unit) {
        eventually {
            expectThat(this).get { state }.assertion()
        }
    }

    override fun receive(updates: List<LobbyNotification>) {
        receivedLobbyNotifications += updates

        updates.forEach {
            state =
                state.run {
                    when (it) {
                        is ACardWasDealt -> copy(hand = hand + it.card)
                        is TheGameHasStarted -> copy(roundNumber = roundNumber.next())
                        is APlayerHasJoined -> copy(players = players + it.playerId)
                        is ABidWasPlaced -> copy(bids = bids + Pair(it.playerId, null))
                        is AllBidsHaveBeenPlaced -> copy(bids = it.bids)
                        is ACardWasPlayed -> copy(cardsInTrick = cardsInTrick + it.playedCard)
                        is TheTrickHasEnded -> copy(trickWinner = it.winner)
                    }
                }
        }
    }

    private val invitedPlayers = mutableListOf<PlayerRole>()

    fun invites(players: List<PlayerRole>) {
        players.forEach { player ->
            player.lobbyId = this.lobbyId
            invitedPlayers += player
        }
    }

    fun invites(vararg players: PlayerRole) {
        require(players.isNotEmpty()) { "Must invite 1 or more players." }
        invites(players.toList())
    }

    operator fun plus(otherPlayers: List<PlayerRole>) = listOf(this) + otherPlayers

    fun `places a bid`(bid: Bid) {
        driver(PlaceABidCommand(lobbyId, id, bid)).orThrow()
    }

    fun `sees that a bid has been placed by`(playerId: PlayerId) {
        hasLobbyStateWhere {
            bids.isNotEmpty().hasEntry(playerId, null)
        }
    }

    fun `see a bid`(
        bid: Bid,
        placedBy: PlayerId,
    ) {
        hasLobbyStateWhere {
            bids.isNotEmpty().hasEntry(placedBy, bid)
        }
    }

    fun `plays a card in their hand`() = `play a card in their hand`()

    fun `play a card in their hand`() {
        driver(PlayACardCommand(lobbyId, id, state.hand.first())).orThrow()
    }

    fun `see that the trick winner has been chosen`() {
        hasLobbyStateWhere {
            trickWinner.isNotNull()
        }
    }

    fun `see a card`(
        card: Card,
        playedBy: PlayerId,
    ) {
        hasLobbyStateWhere {
            cardsInTrick.isNotEmpty().one {
                get { this.playedBy }.isEqualTo(playedBy)
                get { this.card }.isEqualTo(card)
            }
        }
    }

    data class PlayerLobbyState(
        val roundNumber: RoundNumber = RoundNumber.none,
        val hand: List<Card> = emptyList(),
        val players: List<PlayerId> = emptyList(),
        val bids: Map<PlayerId, Bid?> = emptyMap(),
        val cardsInTrick: List<PlayedCard> = emptyList(),
        val trickWinner: PlayerId? = null,
    ) {
        companion object {
            val Builder<PlayerLobbyState>.roundNumber get() = get { roundNumber }.describedAs("round number")
            val Builder<PlayerLobbyState>.hand get() = get { hand }.describedAs("hand")
            val Builder<PlayerLobbyState>.players get() = get { players }.describedAs("players")
            val Builder<PlayerLobbyState>.bids get() = get { bids }.describedAs("bids")
            val Builder<PlayerLobbyState>.cardsInTrick get() = get { cardsInTrick }
            val Builder<PlayerLobbyState>.trickWinner get() = get { trickWinner }
        }
    }

    companion object {
        private fun <T> eventually(block: () -> T): T {
            val stopAt = Instant.now().plusMillis(200)
            var lastError: AssertionError?
            do {
                try {
                    return block()
                } catch (e: AssertionError) {
                    lastError = e
                }
            } while (stopAt > Instant.now())

            throw checkNotNull(lastError)
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> Builder<*>.isA(clazz: Class<T>): Builder<T> =
            assert("is an instance of %s", clazz) {
                when {
                    it == null -> fail(actual = null)
                    it::class.java == clazz -> pass(actual = clazz)
                    else -> fail(actual = it.javaClass)
                }
            } as Builder<T>
    }
}
