package com.tamj0rd2.skullking

import org.junit.jupiter.api.Test

interface EndToEndTestContract {
    fun createPlayerActor(name: String): Player

    @Test
    fun `can play a 2 player game`() {
        val cammy = createPlayerActor("Cammy")

        cammy.`creates a game`()

        // todo: continue until the game is completed.
    }
}
