package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.application.ports.input.GameListItem
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.UL
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.li
import kotlinx.html.stream.createHTML
import kotlinx.html.title
import kotlinx.html.ul

internal fun viewGamesHtml(games: List<GameListItem>): String =
    createHTML().html {
        head {
            scripts()
            title { +"Skull King - Games" }
        }

        body {
            createGameHtml()
            div { +"Games" }
            partialGamesHtml(games)
        }
    }

internal fun FlowContent.partialGamesHtml(games: List<GameListItem>) = ul {
    id = "games"

    games.forEach(::joinableGameHtml)
}

private fun UL.joinableGameHtml(game: GameListItem) {
    val gameId = GameId.show(game.id)
    val hostId = PlayerId.show(game.host)

    li {
        attributes["data-game-id"] = gameId
        attributes["data-host-id"] = hostId
        +"Game $gameId hosted by $hostId"
    }
}

private fun FlowContent.createGameHtml() {
    form {
        input {
            type = InputType.text
            name = "player_id"
            placeholder = "Player ID"
        }

        button {
            attributes["hx-post"] = "/games"
            attributes["hx-target"] = "#games"
            attributes["hx-swap"] = "outerHTML"
            +"Create Game"
        }
    }
}
