package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.GamePhase
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.MAIN
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.html.fieldSet
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.header
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.legend
import kotlinx.html.li
import kotlinx.html.main
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

        highLevelRoundInfo(state)

        when (state.phase) {
            GamePhase.WaitingForPlayers -> {}
            GamePhase.Bidding -> if (state.myBid == null) biddingForm() else placedBid(state.myBid)
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

private fun MAIN.placedBid(myBid: Bid) {
    h3 {
        +"You bid "
        span {
            attributes["data-testid"] = "player-bid"
            +"${myBid.toInt()}"
        }
    }
}

private fun MAIN.highLevelRoundInfo(state: PlayerSpecificGameState) {
    header {
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
    }
}

private fun MAIN.biddingForm() {
    form {
        attributes["ws-send"] = ""
        attributes["x-data"] = "{ bid: 0, defaultBid: ${Bid.min.toInt()}, maxBid: ${Bid.max.toInt()} }"

        h3 { +"Place Your Bid" }

        fieldSet {
            legend { +"Number of tricks you think you'll win:" }

            button(type = ButtonType.button) {
                attributes["x-on:click"] = "bid > defaultBid ? bid-- : bid"
                +"-"
            }

            input(type = InputType.text) {
                name = "bid"
                attributes["x-bind:value"] = "bid"
                attributes["inputmode"] = "numeric"
                attributes["pattern"] = "[0-9]*"
            }

            button(type = ButtonType.button) {
                attributes["x-on:click"] = "bid < maxBid ? bid++ : bid"
                +"+"
            }
        }

        input(type = InputType.hidden) {
            name = "_action"
            value = "PlaceBid"
        }

        input(type = InputType.submit) { value = "Place Bid" }
    }
}
