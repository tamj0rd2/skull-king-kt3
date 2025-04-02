package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameErrorCode.NotEnoughPlayersToCreateGame
import com.tamj0rd2.skullking.domain.game.GameErrorCode.TooManyPlayersToCreateGame
import com.tamj0rd2.skullking.domain.game.PropertyTesting.propertyTest
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.failureOrNull
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test

class CreateGameTest {
    @Test
    fun `cannot create a game without 2-6 players`() {
        propertyTest {
            val playerIdsArb =
                Arb.choice(
                    validPlayerIdsArb,
                    potentiallyInvalidPlayerIdsArb,
                )

            playerIdsArb.checkAll { players ->
                val gameResult = Game.new(players)

                when {
                    players.size < 2 -> assert(gameResult.failureOrNull() is NotEnoughPlayersToCreateGame)
                    players.size > 6 -> assert(gameResult.failureOrNull() is TooManyPlayersToCreateGame)
                    else -> assert(gameResult is Success)
                }
            }
        }
    }
}
