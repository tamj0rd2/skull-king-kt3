package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import com.tamj0rd2.skullking.domain.game.PlayerId
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.li
import kotlinx.html.main
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
    main {
        attributes["id"] = "game"

        h1 { +"Game" }

        h2 { +"Players" }
        ul {
            id = "players"

            state.players.forEach { li { +PlayerId.show(it) } }
        }
    }
}
