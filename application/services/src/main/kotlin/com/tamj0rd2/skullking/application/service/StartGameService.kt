package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameOutput
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.domain.model.game.Card
import com.tamj0rd2.skullking.domain.model.game.CardDealtEvent
import com.tamj0rd2.skullking.domain.model.game.GameAction
import com.tamj0rd2.skullking.domain.model.game.GameCreatedEvent
import com.tamj0rd2.skullking.domain.model.game.GameErrorCode
import com.tamj0rd2.skullking.domain.model.game.GameEvent
import com.tamj0rd2.skullking.domain.model.game.GameStartedEvent
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import com.tamj0rd2.skullking.domain.model.game.PlayerJoinedEvent
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure

class StartGameService(
    private val gameRepository: GameRepository,
    private val gameUpdateNotifier: GameUpdateNotifier,
) : StartGameUseCase {
    override fun invoke(command: StartGameCommand): Result4k<StartGameOutput, GameErrorCode> {
        val game = gameRepository.load(command.gameId)
        game.execute(GameAction.Start).onFailure { return it }
        gameRepository.save(game)

        gameUpdateNotifier.broadcast(game.id, game.newEvents.toGameUpdates())
        return StartGameOutput.asSuccess()
    }

    private fun List<GameEvent>.toGameUpdates() =
        map {
            when (it) {
                is GameCreatedEvent -> TODO()
                is GameStartedEvent -> GameUpdate.GameStarted
                is PlayerJoinedEvent -> TODO()
                is CardDealtEvent -> GameUpdate.CardDealt(Card)
            }
        }
}
