package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.LobbyArbs.playerIdArb
import com.tamj0rd2.skullking.domain.game.LobbyArbs.validBid
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.choose
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.set

object LobbyCommandArbs {
    private val addPlayerLobbyCommandArb =
        arbitrary {
            LobbyCommand.AddPlayer(
                playerId = playerIdArb.bind(),
            )
        }

    private val startGameLobbyCommandArb =
        arbitrary {
            LobbyCommand.StartGame
        }

    private val placeBidLobbyCommandArb =
        arbitrary {
            LobbyCommand.PlaceBid(playerIdArb.bind(), Arb.validBid.bind())
        }

    private val playACardLobbyCommandArb =
        arbitrary {
            LobbyCommand.PlayACard(playerIdArb.bind(), Card)
        }

    private val lobbyCommandArb =
        Arb.choice(
            addPlayerLobbyCommandArb,
            startGameLobbyCommandArb,
            placeBidLobbyCommandArb,
            playACardLobbyCommandArb,
        )

    private val possiblyInvalidLobbyCommandsArb = Arb.list(lobbyCommandArb)

    val validLobbyCommandsArb =
        arbitrary {
            buildList {
                val addPlayerActions = Arb.set(addPlayerLobbyCommandArb, Lobby.MINIMUM_PLAYER_COUNT..<Lobby.MAXIMUM_PLAYER_COUNT).bind()
                addAll(addPlayerActions)

                add(startGameLobbyCommandArb.bind())

                // FIXME: this action stuff sucks now :(
                val bidPlacedActions =
                    Arb
                        .set(placeBidLobbyCommandArb, addPlayerActions.size, addPlayerActions.size)
                        .bind()
                        .zip(addPlayerActions)
                        .map { (a, b) -> a.copy(playerId = b.playerId) }
                addAll(bidPlacedActions)

                val playCardActions =
                    Arb
                        .set(playACardLobbyCommandArb, addPlayerActions.size, addPlayerActions.size)
                        .bind()
                        .zip(addPlayerActions)
                        .map { (a, b) -> a.copy(playerId = b.playerId) }
                addAll(playCardActions)
            }
        }

    val lobbyCommandsArb =
        Arb.choose(
            5 to validLobbyCommandsArb,
            1 to possiblyInvalidLobbyCommandsArb,
        )
}

fun Lobby.mustExecute(action: LobbyCommand) = execute(action).orThrow()
