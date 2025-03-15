package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameErrorCode.NotEnoughPlayersToStartGame
import com.tamj0rd2.skullking.domain.game.GameEvent.GameStarted
import dev.forkhandles.values.LongValueFactory
import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.random
import java.util.UUID

sealed class GameInProgressCommand

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

sealed interface GameEvent {
    val gameId: GameId

    data class GameStarted(
        override val gameId: GameId,
    ) : GameEvent

    data class RoundStarted(
        override val gameId: GameId,
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

class Game(
    private val players: List<PlayerId>,
    val events: MutableList<GameEvent>,
) {
    init {
        if (players.size < 2) throw NotEnoughPlayersToStartGame()
    }

    val gameId = events.single { it is GameStarted }.gameId

    constructor(players: List<PlayerId>) : this(
        players = players,
        events = mutableListOf(GameStarted(GameId.random())),
    )

    init {
        events.forEach(::appendEvent)
    }

    private fun appendEvent(event: GameEvent) {
    }
}

sealed class GameErrorCode : IllegalStateException() {
    class NotEnoughPlayersToStartGame : GameErrorCode()
}
