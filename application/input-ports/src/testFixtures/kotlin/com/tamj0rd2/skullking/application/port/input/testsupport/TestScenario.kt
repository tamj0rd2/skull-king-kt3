package com.tamj0rd2.skullking.application.port.input.testsupport

import com.tamj0rd2.skullking.domain.game.listOfSize

interface TestScenario {
    fun newPlayer(): PlayerRole

    fun newPlayers(count: Int): List<PlayerRole> = listOfSize(count, ::newPlayer)
}

fun List<PlayerRole>.each(block: PlayerRole.() -> Unit) = onEach(block)
