package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.choose
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.set

object GameActionArbs {
    private val addPlayerGameActionArb =
        arbitrary {
            GameAction.AddPlayer(
                playerId = GameArbs.playerIdArb.bind(),
            )
        }

    private val startGameActionArb =
        arbitrary {
            GameAction.Start
        }

    private val gameActionArb =
        Arb.choice(
            addPlayerGameActionArb,
            startGameActionArb,
        )

    private val possiblyInvalidGameActionsArb = Arb.list(gameActionArb)

    val validGameActionsArb =
        arbitrary {
            buildList {
                addAll(Arb.set(addPlayerGameActionArb, Game.MINIMUM_PLAYER_COUNT..<Game.MAXIMUM_PLAYER_COUNT).bind())
                add(startGameActionArb.bind())
            }
        }

    val gameActionsArb =
        Arb.choose(
            5 to validGameActionsArb,
            1 to possiblyInvalidGameActionsArb,
        )
}

fun Game.mustExecute(action: GameAction) = execute(action).orThrow()
