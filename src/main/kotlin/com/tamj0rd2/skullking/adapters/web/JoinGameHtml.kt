package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.domain.game.GameId
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.lang
import kotlinx.html.meta
import kotlinx.html.span
import kotlinx.html.title

fun joinGameHtml(gameId: GameId): String =
    createHTMLDocument()
        .html {
            lang = "en"
            head {
                meta(charset = "UTF-8")
                meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                title("Skull King - Join Game")
                styles()
                scripts()
            }
            body {
                div {
                    div {
                        a(href = "/games") {
                            attributes["hx-boost"] = "true"
                            +"← Back to Games List"
                        }
                    }

                    header { h1 { +"Join Game" } }

                    div {
                        form {
                            gameSettingsSection()
                            div {
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
            }
        }
        .serialize(true)

private fun FlowContent.gameSettingsSection() {
    div {
        h2 {
            span { +"🎮" }
            +"Settings"
        }
        div {
            div {
                label {
                    htmlFor = "playerId"
                    +"Player ID"
                }
                input(type = InputType.text) {
                    id = "playerId"
                    name = "playerId"
                    maxLength = "50"
                    placeholder = "Enter player id..."
                }
            }
        }
    }
}
