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
                div(classes = "container") {
                    header(classes = "header") {
                        h1(classes = "title") { +"Skull King" }
                        p(classes = "subtitle") { +"Join the ultimate card battle" }
                    }

                    div(classes = "actions") {
                        button(classes = "btn btn-primary") {
                            attributes["hx-get"] = "/games/new"
                            attributes["hx-target"] = "body"

                            span { +"➕" }
                            +"Create Game"
                        }
                        button(classes = "btn btn-secondary") {
                            span { +"🔄" }
                            +"Refresh"
                        }
                    }

                    div(classes = "lobby-grid") {
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

    div(classes = "lobby-card") {
        attributes["data-game-id"] = game.id.forDisplay()
        attributes["data-host-id"] = game.host.forDisplay()

        div(classes = "lobby-header") {
            h3(classes = "lobby-name") { +"${game.host.forDisplay()}'s game" }
            span(classes = "lobby-status ${getStatusClass(gameStatus)}") { +getStatusText(gameStatus) }
        }

        div(classes = "lobby-info") {
            div(classes = "info-item") {
                span(classes = "info-label") { +"Players" }
                span(classes = "info-value") { +"${players.size}/$maxPlayers" }
            }
            div(classes = "info-item") {
                span(classes = "info-label") { +"Rounds" }
                span(classes = "info-value") { +rounds.toString() }
            }
            div(classes = "info-item") {
                span(classes = "info-label") { +"Host" }
                span(classes = "info-value") { +game.host.forDisplay() }
            }
        }

        div(classes = "players-list") {
            h4(classes = "players-title") { +"Players" }
            div(classes = "player-tags") { players.forEach { player -> span(classes = "player-tag") { +player } } }
        }

        div(classes = "lobby-actions") {
            button(classes = "btn btn-primary btn-small") {
                attributes["hx-get"] = "/games/${game.id.forDisplay()}/join"
                attributes["hx-target"] = "body"
                +"Join Game"
            }
            button(classes = "btn btn-secondary btn-small") {
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
