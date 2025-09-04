package com.tamj0rd2.skullking

import com.tamj0rd2.skullking.domain.game.RoundNumber
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(value = ExecutionMode.SAME_THREAD)
interface EndToEndTestContract {
    fun createPlayerActor(name: String): Player

    @Test
    fun `can play a 2 player game`() {
        val cammy = createPlayerActor("Cammy")
        val ellis = createPlayerActor("Ellis")

        cammy.`creates a game`()
        cammy.`sees players in the game`(cammy)

        ellis.`joins a game`()
        ellis.`sees players in the game`(cammy, ellis)
        cammy.`sees players in the game`(cammy, ellis)

        cammy.`starts the game`()
        cammy.`sees the round number`(RoundNumber.One)
        ellis.`sees the round number`(RoundNumber.One)

        // todo: continue until the game is completed.
    }
}
