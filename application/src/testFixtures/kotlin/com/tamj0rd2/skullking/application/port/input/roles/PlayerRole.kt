package com.tamj0rd2.skullking.application.port.input.roles

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.Card
import com.tamj0rd2.skullking.domain.model.game.GameId
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import com.tamj0rd2.skullking.domain.model.game.GameUpdate.CardDealt
import com.tamj0rd2.skullking.domain.model.game.GameUpdate.GameStarted
import com.tamj0rd2.skullking.domain.model.game.GameUpdate.PlayerJoined
import com.tamj0rd2.skullking.domain.model.game.RoundNumber
import com.tamj0rd2.testhelpers.eventually
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.ZERO
import strikt.api.Assertion.Builder
import strikt.api.expectThat
import strikt.assertions.isNotEqualTo

class PlayerRole(
    private val driver: SkullKingUseCases,
) : GameUpdateListener {
    var id = PlayerId.ZERO
        private set

    private val playerNumber = shortPlayerId()

    override fun toString(): String = "Player $playerNumber (${id.value})"

    private var gameId = GameId.NONE

    private val receivedGameUpdates = mutableListOf<GameUpdate>()

    fun createsAGame(): GameId = driver(CreateNewGameCommand).gameId

    fun joinsAGame(gameId: GameId): PlayerId {
        this.gameId = gameId
        val command = JoinGameCommand(gameId = gameId, gameUpdateListener = this)
        return driver(command).orThrow().playerId.also {
            expectThat(it).isNotEqualTo(PlayerId.ZERO)
            id = it
        }
    }

    fun startsTheGame() {
        driver(StartGameCommand(gameId, id)).orThrow()
    }

    private var state = PlayerGameState()

    fun gameState(assertion: Builder<PlayerGameState>.() -> Unit) {
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

    data class PlayerGameState(
        val roundNumber: RoundNumber = RoundNumber.none,
        val hand: List<Card> = emptyList(),
        val players: List<PlayerId> = emptyList(),
    ) {
        companion object {
            val Builder<PlayerGameState>.roundNumber get() = get { roundNumber }
            val Builder<PlayerGameState>.hand get() = get { hand }
            val Builder<PlayerGameState>.players get() = get { players }
        }
    }

    companion object {
        private var playerNumber = 0

        fun shortPlayerId() = ++playerNumber
    }
}
