package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.CompleteGame
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.CompleteRound
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.CompleteTrick
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.PlaceABid
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.PlayACard
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.StartTrick
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.GameIdMismatch
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.NotEnoughPlayersToCreateGame
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.TooManyPlayersToCreateGame
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.CardPlayed
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.GameCompleted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.GameStarted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.TrickCompleted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.TrickStarted
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.random
import java.util.Objects
import java.util.UUID

data class GameId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<GameId>(::GameId)
}

class Game private constructor(
    val id: GameId,
) {
    override fun equals(other: Any?): Boolean = other is Game && other.id == id

    override fun hashCode(): Int = Objects.hash(id)

    var state = GameState.new
        private set

    private val _events = mutableListOf<GameEvent>()
    val events get() = _events.toList()

    fun execute(command: GameCommand): Result4k<Unit, GameErrorCode> =
        when (command) {
            is StartRound -> {
                appendEvent(
                    RoundStarted(
                        gameId = id,
                        roundNumber = command.roundNumber,
                        dealtCards = CardsPerPlayer(state.players.associateWith { setOf(CannedCard) }),
                    ),
                )
            }

            is PlaceABid ->
                appendEvent(
                    BidPlaced(
                        gameId = id,
                        bid = command.bid,
                        placedBy = command.actor,
                    ),
                )

            is StartTrick ->
                appendEvent(
                    TrickStarted(
                        gameId = id,
                        trickNumber = command.trickNumber,
                    ),
                )

            is PlayACard ->
                appendEvent(
                    CardPlayed(
                        gameId = id,
                        card = command.card,
                        playedBy = command.actor,
                    ),
                )

            is CompleteTrick ->
                appendEvent(
                    TrickCompleted(
                        gameId = id,
                        trickNumber = command.trickNumber,
                    ),
                )

            is CompleteRound ->
                appendEvent(
                    RoundCompleted(
                        gameId = id,
                        roundNumber = command.roundNumber,
                    ),
                )

            is CompleteGame ->
                appendEvent(
                    GameCompleted(
                        gameId = id,
                    ),
                )
        }

    private fun appendEvent(event: GameEvent): Result4k<Unit, GameErrorCode> {
        if (event.gameId != id) return GameIdMismatch.asFailure()
        state = state.applyEvent(event).onFailure { return it }
        _events.add(event)
        return Unit.asSuccess()
    }

    companion object {
        const val MINIMUM_PLAYER_COUNT = 2
        const val MAXIMUM_PLAYER_COUNT = 6

        fun new(players: Set<PlayerId>): Result<Game, GameErrorCode> {
            if (players.size < MINIMUM_PLAYER_COUNT) return NotEnoughPlayersToCreateGame.asFailure()
            if (players.size > MAXIMUM_PLAYER_COUNT) return TooManyPlayersToCreateGame.asFailure()
            val game = Game(GameId.random())
            game.appendEvent(GameStarted(gameId = game.id, players = players)).onFailure { return it }
            return game.asSuccess()
        }

        fun reconstituteFrom(events: List<GameEvent>): Result<Game, GameErrorCode> {
            val game = Game(events.first().gameId)
            events.forEach { event -> game.appendEvent(event).onFailure { return it } }
            return game.asSuccess()
        }
    }
}
