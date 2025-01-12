package com.tamj0rd2.skullking.application.port.inandout

import com.tamj0rd2.skullking.domain.game.GameArbs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice

object GameUpdateArbs {
    private val playerJoinedGameUpdateArb =
        arbitrary {
            val playerId = GameArbs.playerIdArb.bind()
            GameUpdate.PlayerJoined(playerId)
        }

    val gameUpdateArb =
        Arb.choice(
            playerJoinedGameUpdateArb,
        )
}
