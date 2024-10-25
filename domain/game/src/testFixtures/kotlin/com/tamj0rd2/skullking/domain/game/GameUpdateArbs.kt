package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameArbs.playerIdArb
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice

object GameUpdateArbs {
    private val playerJoinedGameUpdateArb =
        arbitrary {
            val playerId = playerIdArb.bind()
            GameUpdate.PlayerJoined(playerId)
        }

    val gameUpdateArb =
        Arb.choice(
            playerJoinedGameUpdateArb,
        )
}
