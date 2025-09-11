package com.tamj0rd2.skullking.adapters.web

import kotlinx.html.FORM
import kotlinx.html.HEADER
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.header
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.main

fun createGameHtml(): String =
    createHTMLDocument()
        .html {
            common("Skull King - Create Game")

            body {
                header { gamesListLink() }

                main(classes = "container") {
                    header { h1 { +"Create Game" } }

                    form {
                        playerIdInput()

                        button {
                            attributes["hx-post"] = "/games/new"
                            attributes["hx-target"] = "body"
                            attributes["hx-swap"] = "outerHTML"

                            +"Create Game"
                        }
                    }
                }
            }
        }
        .serialize(true)

internal fun HEADER.gamesListLink() {
    a(href = "/games") {
        attributes["hx-boost"] = "true"
        +"Back to Games List"
    }
}

internal fun FORM.playerIdInput() {
    label {
        +"Player ID"

        input(type = InputType.text) {
            id = "playerId"
            name = "playerId"
            maxLength = "50"
            placeholder = "Enter player id..."
        }
    }
}
