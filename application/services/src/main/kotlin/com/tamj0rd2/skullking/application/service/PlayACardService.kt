package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardCommand
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardOutput
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import dev.forkhandles.result4k.Result4k

class PlayACardService : PlayACardUseCase {
    override fun invoke(command: PlayACardCommand): Result4k<PlayACardOutput, GameErrorCode> {
        TODO("Not yet implemented")
    }
}
