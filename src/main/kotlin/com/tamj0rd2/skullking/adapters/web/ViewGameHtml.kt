package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.id
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

        h2 { +"Players" }

        ul(classes = "player-list") {
            id = "players"

            state.players.forEach { playerId ->
                li(classes = "player-item") {
                    span(classes = "player-name") { +playerId.forDisplay() }
                }
            }
        }
    }
}
