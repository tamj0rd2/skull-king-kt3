package com.tamj0rd2.skullking

import org.junit.jupiter.api.Test

interface EndToEndTestContract {
    fun createPlayerActor(name: String): Player

    @Test
    fun `can play a 2 player game`() {
        val cammy = createPlayerActor("Cammy")
        val ellis = createPlayerActor("Ellis")

        cammy.`creates a game`()
        cammy.`sees that the game has been created`()
        cammy.`joins a game`()
        cammy.`sees players in the game`(cammy)

        ellis.`joins a game`()
        ellis.`sees players in the game`(cammy, ellis)
        cammy.`sees players in the game`(cammy, ellis)
        // todo: continue until the game is completed.
    }
}
