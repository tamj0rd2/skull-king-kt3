package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.model.game.Game
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.choose
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set

object GameActionArbs {
    private val addPlayerGameActionArb =
        arbitrary {
            val playerId = GameArbs.playerIdArb.bind()
            GameAction("add player $playerId") { addPlayer(playerId) }
        }

    private val startGameActionArb =
        arbitrary {
            GameAction("start game") { start() }
        }

    private val gameActionArb =
        Arb.choice(
            addPlayerGameActionArb,
            startGameActionArb,
        )

    private val possiblyInvalidGameActionsArb = Arb.list(gameActionArb).map { GameActions(actions = it) }

    val validGameActionsArb =
        arbitrary {
            GameActions(
                actions =
                    buildList {
                        addAll(Arb.set(addPlayerGameActionArb, Game.MINIMUM_PLAYER_COUNT..Game.MAXIMUM_PLAYER_COUNT).bind())
                        add(startGameActionArb.bind())
                    },
            )
        }

    val gameActionsArb =
        Arb.choose(
            5 to validGameActionsArb,
            1 to possiblyInvalidGameActionsArb,
        )
}
