package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.CardDealt
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.GameStarted
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameOutput
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.domain.game.Card
import com.tamj0rd2.skullking.domain.game.CardDealtEvent
import com.tamj0rd2.skullking.domain.game.GameAction
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import com.tamj0rd2.skullking.domain.game.GameEvent
import com.tamj0rd2.skullking.domain.game.GameStartedEvent
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

        gameUpdateNotifier.broadcast(game.id, game.newEventsSinceGameWasLoaded.toGameUpdates())
        return StartGameOutput.asSuccess()
    }

    private fun List<GameEvent>.toGameUpdates() =
        map {
            when (it) {
                is GameStartedEvent -> GameStarted
                is CardDealtEvent -> CardDealt(Card)
                else -> error("unexpected game event at this point in time.")
            }
        }
}

