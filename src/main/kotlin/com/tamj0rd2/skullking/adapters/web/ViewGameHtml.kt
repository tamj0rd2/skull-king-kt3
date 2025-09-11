package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.GamePhase
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.li
import kotlinx.html.main
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.ul
import org.http4k.core.Uri

fun viewGameHtml(joinGameUri: Uri): String =
    createHTMLDocument()
        .div {
            attributes["hx-ext"] = "ws"
            attributes["ws-connect"] = joinGameUri.toString()

            main {
                attributes["id"] = "game"
                +"Loading game..."
            }
        }
        .serialize(true)

fun FlowContent.partialGameState(state: PlayerSpecificGameState) {
    main {
        attributes["id"] = "game"
        attributes["data-game-id"] = state.gameId.forDisplay()

        div { highLevelRoundInfo(state) }

        when (state.phase) {
            GamePhase.WaitingForPlayers -> {}
            GamePhase.Bidding -> {
                if (state.myBid == null) {
                    div {
                        h3 { +"Place Your Bid" }
                        biddingControls()
                    }
                } else {
                    div {
                        h3 {
                            +"You bid "
                            span {
                                attributes["data-testid"] = "player-bid"
                                +"${state.myBid.toInt()}"
                            }
                        }

                        p { +"Waiting for other players to finish bidding..." }
                    }
                }
            }
        }

        h2 { +"Players" }

        ul {
            id = "players"

            state.players.forEach { playerId -> li { span { +playerId.forDisplay() } } }
        }

        form {
            attributes["ws-send"] = ""
            input(type = InputType.hidden) {
                name = "_action"
                value = "StartGame"
            }

            input(type = InputType.submit) { value = "Start Game" }
        }
    }
}

private fun FlowContent.highLevelRoundInfo(state: PlayerSpecificGameState) {
    div {
        h1 {
            when (state.phase) {
                GamePhase.WaitingForPlayers -> +"Lobby"

                else -> {
                    +"Round "
                    span {
                        attributes["data-testid"] = "round-number"
                        +state.roundNumber!!.toInt().toString()
                    }
                }
            }
        }

        when (state.phase) {
            GamePhase.WaitingForPlayers -> div { span { +"Waiting for host to start the game..." } }

            GamePhase.Bidding -> div { span { +"Waiting for all bids..." } }
        }
    }
}

private fun FlowContent.biddingControls() {
    form {
        val defaultBid = Bid.min.toInt()
        val maxBid = Bid.max.toInt()

        attributes["ws-send"] = ""
        attributes["x-data"] = "{ bid: $defaultBid }"

        div {
            div { +"Number of tricks you think you'll win:" }
            div {
                button {
                    attributes["x-on:click"] = "bid > $defaultBid ? bid-- : bid"
                    type = ButtonType.button
                    +"-"
                }

                div { attributes["x-text"] = "bid" }
                button {
                    attributes["x-on:click"] = "bid < $maxBid ? bid++ : bid"
                    type = ButtonType.button
                    +"+"
                }
            }
        }

        input(type = InputType.hidden) {
            attributes["x-bind:value"] = "bid"
            name = "bid"
        }

        input(type = InputType.hidden) {
            name = "_action"
            value = "PlaceBid"
        }

        input(type = InputType.submit) { value = "Place Bid" }
    }
}
