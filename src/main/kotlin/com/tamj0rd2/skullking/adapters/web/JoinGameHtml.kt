package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.domain.game.GameId
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
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
import kotlinx.html.stream.createHTML
import kotlinx.html.title

fun joinGameHtml(gameId: GameId): String =
    createHTML().html {
        lang = "en"
        head {
            meta(charset = "UTF-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
            title("Skull King - Join Game")
            styles()
            scripts()
        }
        body {
            div(classes = "container") {
                div(classes = "back-nav") {
                    a(href = "/games", classes = "back-link") {
                        attributes["hx-boost"] = "true"
                        +"← Back to Games List"
                    }
                }

                header(classes = "header") { h1(classes = "title") { +"Join Game" } }

                div(classes = "form-card") {
                    form {
                        gameSettingsSection()
                        div(classes = "form-actions") {
                            button(classes = "btn btn-primary") {
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

private fun FlowContent.gameSettingsSection() {
    div(classes = "form-section") {
        h2(classes = "section-title") {
            span { +"🎮" }
            +"Settings"
        }
        div(classes = "form-grid") {
            div(classes = "form-group") {
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
