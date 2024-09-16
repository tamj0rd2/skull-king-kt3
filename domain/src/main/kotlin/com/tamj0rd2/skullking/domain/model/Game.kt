package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.random
import java.util.*

@JvmInline
value class GameId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<GameId>(::GameId)
}

class Game private constructor(
    val id: GameId,
) {
    private var initialized = false

    val changes: List<GameEvent>
        field = mutableListOf<GameEvent>()

    val players: List<PlayerId>
        field = mutableListOf<PlayerId>()

    fun addPlayer(playerId: PlayerId): Result4k<Unit, AddPlayerErrorCode> {
        if (players.size >= MAXIMUM_PLAYER_COUNT) return GameIsFull.asFailure()
        players.add(playerId)
        recordEvent(PlayerJoined(id, playerId))
        return Unit.asSuccess()
    }

    private fun recordEvent(event: GameEvent) {
        if (initialized) changes.add(event)
    }

    companion object {
        const val MAXIMUM_PLAYER_COUNT = 6

        fun new() =
            Game(GameId.random()).apply {
                initialized = true
                changes.add(GameCreated(id))
            }

        fun from(history: List<GameEvent>): Game {
            check(history.isNotEmpty()) { "Provided history was empty. Create a new game instead." }

            val gameId = history.first().gameId

            return Game(gameId).apply {
                check(history.all { it.gameId == this.id }) { "GameId mismatch" }
                history.forEach { event ->
                    when (event) {
                        is PlayerJoined -> addPlayer(event.playerId)
                        is GameCreated -> Unit.asSuccess()
                    }.orThrow()
                }
                initialized = true
            }
        }
    }
}

sealed class GameErrorCode : RuntimeException()

sealed class AddPlayerErrorCode : GameErrorCode()

data object GameIsFull : AddPlayerErrorCode() {
    @Suppress("unused")
    private fun readResolve(): Any = GameIsFull
}
