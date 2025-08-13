package com.tamj0rd2.skullking

import org.junit.jupiter.api.Test
import org.junit.platform.commons.annotation.Testable

@Testable
interface EndToEndTestContract {
    fun createPlayerActor(): Player

    @Test
    fun `can play a 2 player game`() {
        val cammy = createPlayerActor()
        val ellis = createPlayerActor()

        cammy.`creates a game`()
        ellis.`joins a game`()

        // todo: continue until the game is completed.
    }
}
