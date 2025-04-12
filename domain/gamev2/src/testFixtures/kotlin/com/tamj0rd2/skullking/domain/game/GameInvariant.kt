package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.checkAll

fun interface GameInvariant {
    operator fun invoke(game: Game)
}

@Deprecated("do not use.")
fun gameInvariant(invariant: GameInvariant) {
    propertyTest {
        checkAll(ptConfig(), Arb.validPlayerIds, Arb.gameCommands) { playerIds, gameCommands ->
            val game = Game.new(playerIds).orThrow()

            gameCommands.forEach { command ->
                game.execute(command)
                invariant(game)
            }
        }
    }
}
