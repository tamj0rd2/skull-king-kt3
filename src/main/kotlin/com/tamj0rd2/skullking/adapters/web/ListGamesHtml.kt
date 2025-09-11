package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.application.ports.input.GameListItem
import kotlinx.html.FlowContent
import kotlinx.html.article
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.html.footer
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.h4
import kotlinx.html.header
import kotlinx.html.html
import kotlinx.html.li
import kotlinx.html.main
import kotlinx.html.span
import kotlinx.html.ul

fun listGamesHtml(games: List<GameListItem>): String =
    createHTMLDocument()
        .html {
            common("Skull King - Games")

            body {
                header {
                    h1 { +"Skull King" }

                    button {
                        attributes["hx-get"] = "/games/new"
                        attributes["hx-target"] = "body"

                        +"Create Game"
                    }
                }

                main(classes = "container") {
                    if (games.isNotEmpty()) {
                        article {
                            h2 { +"Games" }
                            attributes["id"] = "games"
                            games.forEach(::gameCard)
                        }
                    }
                }
            }
        }
        .serialize(true)

private fun FlowContent.gameCard(game: GameListItem) {
    val players = listOf("TODO", "TODO", "TODO")

    article {
        attributes["data-game-id"] = game.id.forDisplay()
        attributes["data-host-id"] = game.host.forDisplay()

        header {
            h3 {
                span {
                    attributes["data-testid"] = "host"
                    +game.host.forDisplay()
                }

                +"'s game"
            }
        }

        h4 { +"Players" }
        ul { players.forEach { player -> li { +player } } }

        footer {
            button {
                attributes["hx-get"] = "/games/${game.id.forDisplay()}/join"
                attributes["hx-target"] = "body"
                +"Join Game"
            }
        }
    }
}
