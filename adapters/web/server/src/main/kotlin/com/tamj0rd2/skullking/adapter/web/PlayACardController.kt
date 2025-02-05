package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlayACardMessage
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardCommand
import dev.forkhandles.result4k.orThrow

// TODO: write some kind of ClientMessageHandler interface. Always takes a playerSession and a message of type T.
//  It shouldn't return anything. It should handle its own errors.
internal class PlayACardController(
    private val playACardUseCase: PlayACardUseCase,
) : MessageReceiver<PlayACardMessage> {
    override operator fun invoke(
        session: PlayerSession,
        message: PlayACardMessage,
    ) {
        val command =
            PlayACardCommand(
                lobbyId = session.lobbyId,
                playerId = session.playerId,
                card = message.card,
            )

        playACardUseCase.invoke(command).orThrow()
    }
}
