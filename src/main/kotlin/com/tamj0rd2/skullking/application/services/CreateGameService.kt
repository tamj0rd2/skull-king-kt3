package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.input.CreateGameInput
import com.tamj0rd2.skullking.application.ports.input.CreateGameOutput
import com.tamj0rd2.skullking.application.ports.input.CreateGameUseCase

class CreateGameService : CreateGameUseCase {
    override fun execute(input: CreateGameInput): CreateGameOutput {
        TODO("Not yet implemented")
    }
}
