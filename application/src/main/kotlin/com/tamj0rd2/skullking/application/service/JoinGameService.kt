package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.GameUpdate
import com.tamj0rd2.skullking.domain.model.PlayerId
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random

class JoinGameService(
    private val gameRepository: GameRepository,
    private val gameUpdateNotifier: GameUpdateNotifier,
) : JoinGameUseCase {
    override fun invoke(command: JoinGameCommand): JoinGameOutput {
        val playerId = PlayerId.random()

        val game = gameRepository.load(command.gameId)
        game.addPlayer(playerId).orThrow()
        gameRepository.save(game)

        gameUpdateNotifier.broadcast(GameUpdate.PlayerJoined(playerId))
        gameUpdateNotifier.subscribe(
            gameId = game.id,
            playerId = playerId,
            listener = command.gameUpdateListener,
        )

        return JoinGameOutput(playerId)
    }
}

interface GameUpdateNotifier {
    fun subscribe(
        gameId: GameId,
        playerId: PlayerId,
        listener: GameUpdateListener,
    )

    fun broadcast(updates: List<GameUpdate>)

    fun broadcast(vararg updates: GameUpdate) {
        require(updates.isNotEmpty()) { "list of updates was empty" }
        return broadcast(updates.toList())
    }
}

class GameUpdateNotifierInMemoryAdapter : GameUpdateNotifier {
    private val listeners = mutableMapOf<Key, GameUpdateListener>()

    override fun subscribe(
        gameId: GameId,
        playerId: PlayerId,
        listener: GameUpdateListener,
    ) {
        listeners[Key(gameId, playerId)] = listener
    }

    override fun broadcast(updates: List<GameUpdate>) {
        listeners.values.forEach { it.send(updates) }
    }

    private data class Key(
        val gameId: GameId,
        val playerId: PlayerId,
    )
}
