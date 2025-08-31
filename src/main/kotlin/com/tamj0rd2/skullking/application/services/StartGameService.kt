package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.input.StartGameInput
import com.tamj0rd2.skullking.application.ports.input.StartGameOutput
import com.tamj0rd2.skullking.application.ports.input.StartGameUseCase
import com.tamj0rd2.skullking.application.ports.output.LoadGamePort
import com.tamj0rd2.skullking.application.ports.output.SaveGamePort
import com.tamj0rd2.skullking.application.ports.output.VersionedAtLoad.Companion.map

class StartGameService(private val saveGamePort: SaveGamePort, private val loadGamePort: LoadGamePort) : StartGameUseCase {
    override fun execute(input: StartGameInput): StartGameOutput {
        val game = loadGamePort.load(input.gameId)!!.map { it.start() }
        saveGamePort.save(game)
        return StartGameOutput
    }
}
