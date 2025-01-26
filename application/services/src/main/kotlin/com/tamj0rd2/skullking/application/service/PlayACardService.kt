package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.inandout.LobbyNotification
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardCommand
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardOutput
import com.tamj0rd2.skullking.application.port.output.LobbyNotifier
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import com.tamj0rd2.skullking.domain.game.PlayedCard
import dev.forkhandles.result4k.Result4k

class PlayACardService(
    private val lobbyNotifier: LobbyNotifier,
) : PlayACardUseCase {
    override fun invoke(command: PlayACardCommand): Result4k<PlayACardOutput, LobbyErrorCode> {
        val playedCard = PlayedCard(command.card, command.playerId)
        lobbyNotifier.broadcast(command.lobbyId, LobbyNotification.ACardWasPlayed(playedCard))

        // FIXME: the winner is wrong. drive out correct behaviour through a test.
        lobbyNotifier.broadcast(command.lobbyId, LobbyNotification.TheTrickHasEnded(winner = command.playerId))
        return PlayACardOutput.asSuccess()
    }
}
