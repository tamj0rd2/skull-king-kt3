package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.extensions.assertFailureIs
import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.NotEnoughPlayersToCreateGame
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.TooManyPlayersToCreateGame
import dev.forkhandles.result4k.Success
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.set
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test

class CreateGameTest {
    @Test
    fun `cannot create a game without 2-6 players`() {
        propertyTest {
            val playerIdsArb =
                Arb.choice(
                    Arb.validPlayerIds,
                    Arb.set(Arb.playerId),
                )

            playerIdsArb.checkAll { players ->
                val gameResult = Game.new(players)

                when {
                    players.size < 2 -> assertFailureIs<NotEnoughPlayersToCreateGame>(gameResult)
                    players.size > 6 -> assertFailureIs<TooManyPlayersToCreateGame>(gameResult)
                    else -> assert(gameResult is Success)
                }
            }
        }
    }
}
