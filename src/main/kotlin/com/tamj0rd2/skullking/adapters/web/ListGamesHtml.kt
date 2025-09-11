package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.application.ports.input.GameListItem
import kotlinx.html.FlowContent
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.html.h1
import kotlinx.html.h3
import kotlinx.html.h4
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.html
import kotlinx.html.lang
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.title

enum class GameStatus {
    TODO,
    WAITING,
    PLAYING,
    FULL,
}

fun listGamesHtml(games: List<GameListItem>): String =
    createHTMLDocument()
        .html {
            lang = "en"
            head {
                meta(charset = "UTF-8")
                meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                title("Skull King - Games")
                styles()
                scripts()
            }
            body {
                div {
                    header {
                        h1 { +"Skull King" }
                        p { +"Join the ultimate card battle" }
                    }

                    div {
                        button {
                            attributes["hx-get"] = "/games/new"
                            attributes["hx-target"] = "body"

                            span { +"➕" }
                            +"Create Game"
                        }
                        button {
                            span { +"🔄" }
                            +"Refresh"
                        }
                    }

                    div {
                        attributes["id"] = "games"
                        games.forEach(::lobbyCard)
                    }
                }
            }
        }
        .serialize(true)

fun FlowContent.lobbyCard(game: GameListItem) {
    val gameStatus = GameStatus.TODO
    val rounds = 10
    val maxPlayers = 6
    val players = listOf("TODO", "TODO", "TODO")

    div {
        attributes["data-game-id"] = game.id.forDisplay()
        attributes["data-host-id"] = game.host.forDisplay()

        div {
            h3 { +"${game.host.forDisplay()}'s game" }
            span { +getStatusText(gameStatus) }
        }

        div {
            div {
                span { +"Players" }
                span { +"${players.size}/$maxPlayers" }
            }
            div {
                span { +"Rounds" }
                span { +rounds.toString() }
            }
            div {
                span { +"Host" }
                span { +game.host.forDisplay() }
            }
        }

        div {
            h4 { +"Players" }
            div { players.forEach { player -> span { +player } } }
        }

        div {
            button {
                attributes["hx-get"] = "/games/${game.id.forDisplay()}/join"
                attributes["hx-target"] = "body"
                +"Join Game"
            }
            button {
                disabled = true
                +"Spectate"
            }
        }
    }
}

private fun getStatusClass(status: GameStatus): String =
    when (status) {
        GameStatus.TODO -> "status-playing"
        GameStatus.WAITING -> "status-waiting"
        GameStatus.PLAYING -> "status-playing"
        GameStatus.FULL -> "status-full"
    }

private fun getStatusText(status: GameStatus): String =
    when (status) {
        GameStatus.TODO -> "TODO"
        GameStatus.WAITING -> "Waiting"
        GameStatus.PLAYING -> "Active"
        GameStatus.FULL -> "Full"
    }
