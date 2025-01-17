package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.game.Card
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k

interface PlayACardUseCase {
    data class PlayACardCommand(
        val gameId: GameId,
        val playerId: PlayerId,
        val card: Card,
    )

    data object PlayACardOutput

    operator fun invoke(command: PlayACardCommand): Result4k<PlayACardOutput, GameErrorCode>
}
