package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.game.GameErrorCode.NotEnoughPlayersToStartGame
import com.tamj0rd2.skullking.domain.game.GameErrorCode.NotImplemented
import com.tamj0rd2.skullking.domain.game.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.game.GameEvent.CardPlayed
import com.tamj0rd2.skullking.domain.game.GameEvent.GameCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.GameStarted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickCompleted
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.LongValueFactory
import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.random
import java.util.UUID

data class GameId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<GameId>(::GameId)
}

data class PlayerId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<PlayerId>(::PlayerId)
}

data class Version private constructor(
    override val value: Long,
) : Value<Long> {
    companion object : LongValueFactory<Version>(::Version) {
        val none = Version(0)
    }
}

data class RoundNumber private constructor(
    override val value: Int,
) : Value<Int> {
    val next: RoundNumber get() = RoundNumber(value + 1)

    companion object : IntValueFactory<RoundNumber>(::RoundNumber) {
        val none = RoundNumber(0)
    }
}

sealed interface Card

data object CannedCard : Card

sealed interface GameEvent {
    val gameId: GameId

    data class GameStarted(
        override val gameId: GameId,
        val players: Set<PlayerId>,
    ) : GameEvent

    data class RoundStarted(
        override val gameId: GameId,
        val roundNumber: RoundNumber,
        val dealtCards: CardsPerPlayer,
    ) : GameEvent

    data class BidPlaced(
        override val gameId: GameId,
    ) : GameEvent

    data class CardPlayed(
        override val gameId: GameId,
    ) : GameEvent

    data class TrickCompleted(
        override val gameId: GameId,
    ) : GameEvent

    data class RoundCompleted(
        override val gameId: GameId,
    ) : GameEvent

    data class GameCompleted(
        override val gameId: GameId,
    ) : GameEvent
}

sealed interface GameCommand {
    data class StartRound(
        val roundNumber: RoundNumber,
    ) : GameCommand
}

class Game private constructor(
    private val gameId: GameId,
) {
    val events = mutableListOf<GameEvent>()

    constructor(players: Set<PlayerId>) : this(
        gameId = GameId.random(),
    ) {
        appendEvent(GameStarted(gameId = gameId, players = players)).orThrow()
    }

    var state = GameState.new
        private set

    fun execute(command: GameCommand): Result4k<Unit, GameErrorCode> =
        when (command) {
            is StartRound ->
                appendEvent(
                    RoundStarted(
                        gameId = gameId,
                        roundNumber = state.roundNumber.next,
                        dealtCards = CardsPerPlayer(state.players.associateWith { setOf(CannedCard) }),
                    ),
                )
        }

    private fun appendEvent(event: GameEvent): Result4k<Unit, GameErrorCode> {
        check(event.gameId == gameId) { "GameId mismatch" }
        state = state.applyEvent(event).onFailure { return it }
        events.add(event)
        return Unit.asSuccess()
    }
}

data class CardsPerPlayer(
    val perPlayer: Map<PlayerId, Set<Card>>,
)

data class GameState(
    val players: Set<PlayerId>,
    val roundNumber: RoundNumber,
    val cardsPerPlayer: CardsPerPlayer,
) {
    fun applyEvent(event: GameEvent): Result4k<GameState, GameErrorCode> {
        return when (event) {
            is BidPlaced -> {
                NotImplemented().asFailure()
            }
            is CardPlayed -> {
                NotImplemented().asFailure()
            }
            is GameCompleted -> {
                NotImplemented().asFailure()
            }
            is GameStarted -> {
                if (event.players.size < 2) return NotEnoughPlayersToStartGame().asFailure()
                return copy(players = event.players).asSuccess()
            }
            is RoundCompleted -> {
                NotImplemented().asFailure()
            }
            is RoundStarted -> {
                return copy(cardsPerPlayer = event.dealtCards).asSuccess()
            }
            is TrickCompleted -> {
                NotImplemented().asFailure()
            }
        }
    }

    companion object {
        val new =
            GameState(
                players = emptySet(),
                roundNumber = RoundNumber.none,
                cardsPerPlayer = CardsPerPlayer(emptyMap()),
            )
    }
}

sealed class GameErrorCode : IllegalStateException() {
    class NotEnoughPlayersToStartGame : GameErrorCode()

    class NotImplemented : GameErrorCode()
}
