package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.PlayerRole.PlayerGameState.Companion.players
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.Card
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.GameUpdate
import com.tamj0rd2.skullking.domain.game.GameUpdate.CardDealt
import com.tamj0rd2.skullking.domain.game.GameUpdate.GameStarted
import com.tamj0rd2.skullking.domain.game.GameUpdate.PlayerJoined
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.domain.game.RoundNumber
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
import strikt.api.Assertion.Builder
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.contains
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.time.Instant

class PlayerRole(
    private val driver: SkullKingUseCases,
) : GameUpdateListener {
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

    override fun toString(): String = "$sessionId ($id)"

    private var gameId = GameId.NONE
        get() {
            expectThat(field).isNotEqualTo(GameId.NONE)
            return field
        }

    private val receivedGameUpdates = mutableListOf<GameUpdate>()

    fun `has created a game`() = `creates a game`()

    fun `creates a game`(): GameId {
        val output =
            driver(
                CreateNewGameCommand(
                    sessionId = sessionId,
                    gameUpdateListener = this,
                ),
            )

        expectThat(output.playerId).isNotEqualTo(PlayerId.NONE)
        id = output.playerId

        expectThat(output.gameId).isNotEqualTo(GameId.NONE)
        this.gameId = output.gameId
        return output.gameId
    }

    fun `accepts the game invite`() = `accept the game invite`()

    fun `accept the game invite`() {
        expectThat(gameId).isNotEqualTo(GameId.NONE)

        val command =
            JoinGameCommand(
                sessionId = sessionId,
                gameId = gameId,
                gameUpdateListener = this,
            )

        driver.invoke(command).orThrow().playerId.also {
            expectThat(it).isNotEqualTo(PlayerId.NONE)
            id = it
        }
    }

    fun `join the game again`() {
        expectThat(gameId).isNotEqualTo(GameId.NONE)
        `accept the game invite`()
    }

    private var latestErrorCode: GameErrorCode? = null

    fun triesTo(block: PlayerRole.() -> Unit) {
        expectThat(latestErrorCode).isNull()

        try {
            @Suppress("UNUSED_EXPRESSION")
            block()
        } catch (e: GameErrorCode) {
            latestErrorCode = e
        }
    }

    fun `gets the error`(expectedErrorCode: GameErrorCode) {
        expectThat(latestErrorCode).isNotNull().isA(expectedErrorCode::class.java)
        latestErrorCode = null
    }

    fun `starts the game`() {
        driver(StartGameCommand(gameId, id)).orThrow()
    }

    private var state = PlayerGameState()

    fun `sees them self in the game`() {
        hasGameStateWhere { players.contains(id) }
    }

    fun `sees each invited player in the game`() {
        expectThat(invitedPlayers.isNotEmpty())
        `sees exact players in the game`(this + invitedPlayers)
    }

    fun `sees exact players in the game`(expected: List<PlayerRole>) {
        hasGameStateWhere {
            players.hasSize(expected.size)
            players.containsExactlyInAnyOrder(expected.map { it.id })
            players.all { isNotEqualTo(PlayerId.NONE) }
        }
    }

    fun hasGameStateWhere(assertion: Builder<PlayerGameState>.() -> Unit) {
        eventually {
            expectThat(this).get { state }.assertion()
        }
    }

    override fun send(updates: List<GameUpdate>) {
        receivedGameUpdates += updates

        updates.forEach {
            state =
                state.run {
                    when (it) {
                        is CardDealt -> copy(hand = hand + it.card)
                        is GameStarted -> copy(roundNumber = roundNumber.next())
                        is PlayerJoined -> copy(players = players + it.playerId)
                    }
                }
        }
    }

    private val invitedPlayers = mutableListOf<PlayerRole>()

    fun invites(players: List<PlayerRole>) {
        players.forEach { player ->
            player.gameId = this.gameId
            invitedPlayers += player
        }
    }

    fun invites(vararg players: PlayerRole) {
        require(players.isNotEmpty()) { "Must invite 1 or more players." }
        invites(players.toList())
    }

    operator fun plus(otherPlayers: List<PlayerRole>) = listOf(this) + otherPlayers

    data class PlayerGameState(
        val roundNumber: RoundNumber = RoundNumber.none,
        val hand: List<Card> = emptyList(),
        val players: List<PlayerId> = emptyList(),
    ) {
        companion object {
            val Builder<PlayerGameState>.roundNumber get() = get { roundNumber }.describedAs("round number")
            val Builder<PlayerGameState>.hand get() = get { hand }.describedAs("hand")
            val Builder<PlayerGameState>.players get() = get { players }.describedAs("players")
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
