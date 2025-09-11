package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.domain.game.GameId
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.header
import kotlinx.html.html
import kotlinx.html.main

fun joinGameHtml(gameId: GameId): String =
    createHTMLDocument()
        .html {
            common("Skull King - Join Game")

            body {
                header { gamesListLink() }

                main(classes = "container") {
                    header { h1 { +"Join Game" } }

                    form {
                        playerIdInput()

                        button {
                            attributes["hx-post"] = "/games/${gameId.forDisplay()}/join"
                            attributes["hx-target"] = "body"
                            attributes["hx-swap"] = "outerHTML"
                            +"Join Game"
                        }
                    }
                }
            }
        }
        .serialize(true)
