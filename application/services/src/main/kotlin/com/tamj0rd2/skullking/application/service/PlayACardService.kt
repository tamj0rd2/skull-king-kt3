package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardCommand
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardOutput
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import com.tamj0rd2.skullking.domain.game.PlayedCard
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k

class PlayACardService(
    private val gameUpdateNotifier: GameUpdateNotifier,
) : PlayACardUseCase {
    override fun invoke(command: PlayACardCommand): Result4k<PlayACardOutput, GameErrorCode> {
        val playedCard = PlayedCard(command.card, command.playerId)
        gameUpdateNotifier.broadcast(command.gameId, GameUpdate.ACardWasPlayed(playedCard))
        gameUpdateNotifier.broadcast(command.gameId, GameUpdate.TheTrickHasEnded(winner = PlayerId.NONE))

        return PlayACardOutput.asSuccess()
    }
}
