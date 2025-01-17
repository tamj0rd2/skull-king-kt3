package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameArbs.validBid
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

    private val placeBidGameActionArb =
        arbitrary {
            GameAction.PlaceBid(GameArbs.playerIdArb.bind(), Arb.validBid.bind())
        }

    private val gameActionArb =
        Arb.choice(
            addPlayerGameActionArb,
            startGameActionArb,
            placeBidGameActionArb,
        )

    private val possiblyInvalidGameActionsArb = Arb.list(gameActionArb)

    val validGameActionsArb =
        arbitrary {
            buildList {
                val addPlayerActions = Arb.set(addPlayerGameActionArb, Game.MINIMUM_PLAYER_COUNT..<Game.MAXIMUM_PLAYER_COUNT).bind()
                addAll(addPlayerActions)

                add(startGameActionArb.bind())

                // FIXME: this action stuff sucks now :(
                val bidPlacedActions =
                    Arb
                        .set(placeBidGameActionArb, addPlayerActions.size, addPlayerActions.size)
                        .bind()
                        .zip(addPlayerActions)
                        .map { (a, b) -> a.copy(playerId = b.playerId) }
                addAll(bidPlacedActions)
            }
        }

    val gameActionsArb =
        Arb.choose(
            5 to validGameActionsArb,
            1 to possiblyInvalidGameActionsArb,
        )
}

fun Game.mustExecute(action: GameAction) = execute(action).orThrow()
