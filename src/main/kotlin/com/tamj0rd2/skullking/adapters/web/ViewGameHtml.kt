package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.li
import kotlinx.html.main
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.ul
import org.http4k.core.Uri

fun viewGameHtml(joinGameUri: Uri): String =
    createHTML().div {
        attributes["hx-ext"] = "ws"
        attributes["ws-connect"] = joinGameUri.toString()

        main {
            attributes["id"] = "game"
            +"Loading game..."
        }
    }

fun FlowContent.partialGameState(state: PlayerSpecificGameState) {
    main(classes = "container") {
        attributes["id"] = "game"
        attributes["data-game-id"] = state.gameId.forDisplay()

        if (state.roundNumber != null) {
            h2 {
                +"Round "
                span {
                    attributes["data-testid"] = "round-number"
                    +state.roundNumber.toInt().toString()
                }
            }
        }

        h2 { +"Players" }

        ul(classes = "player-list") {
            id = "players"

            state.players.forEach { playerId -> li(classes = "player-item") { span(classes = "player-name") { +playerId.forDisplay() } } }
        }

        form {
            attributes["ws-send"] = ""
            input(type = InputType.hidden) {
                name = "action"
                value = "StartGame"
            }

            input(type = InputType.submit, classes = "btn btn-primary") { value = "Start Game" }
        }
    }
}
