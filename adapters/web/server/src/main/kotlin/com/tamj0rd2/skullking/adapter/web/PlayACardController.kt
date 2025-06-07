package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlayACardMessage
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardCommand
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k

// TODO: write some kind of ClientMessageHandler interface. Always takes a playerSession and a
// message of type T.
//  It shouldn't return anything. It should handle its own errors.
internal class PlayACardController(private val playACardUseCase: PlayACardUseCase) :
    MessageReceiver<PlayACardMessage> {
    override fun receive(
        playerId: PlayerId,
        lobbyId: LobbyId,
        message: PlayACardMessage,
    ): Result4k<*, LobbyErrorCode> {
        val command = PlayACardCommand(lobbyId = lobbyId, playerId = playerId, card = message.card)

        return playACardUseCase.invoke(command)
    }
}
