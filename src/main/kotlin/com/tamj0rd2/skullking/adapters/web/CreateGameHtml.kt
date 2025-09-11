package com.tamj0rd2.skullking.adapters.web

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
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.select
import kotlinx.html.span
import kotlinx.html.title

data class LobbySettings(
    val name: String = "",
    val maxPlayers: Int = 6,
    val rounds: Int = 10,
    val password: String = "",
    val description: String = "",
    val allowSpectators: Boolean = true,
    val rankedMode: Boolean = false,
    val quickStart: Boolean = true,
    val rejoinAllowed: Boolean = true,
)

fun createGameHtml(settings: LobbySettings = LobbySettings()): String =
    createHTMLDocument()
        .html {
            lang = "en"
            head {
                meta(charset = "UTF-8")
                meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
                title("Skull King - Create Game")
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

                    header {
                        h1 { +"Create Lobby" }
                        p { +"Set up your game room" }
                    }

                    div {
                        form {
                            gameSettingsSection(settings)
                            formActionsSection()
                        }
                    }
                }
            }
        }
        .serialize(true)

private fun FlowContent.gameSettingsSection(settings: LobbySettings) {
    div {
        h2 {
            span { +"🎮" }
            +"Game Settings"
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
                    value = settings.name
                }
                div { +"Choose a memorable name for your game" }
            }
            div {
                label {
                    htmlFor = "max-players"
                    +"Max Players"
                }
                select {
                    id = "max-players"
                    (3..8).forEach { playerCount ->
                        option {
                            value = playerCount.toString()
                            if (playerCount == settings.maxPlayers) selected = true
                            +"$playerCount Players"
                        }
                    }
                }
            }
        }
        div {
            div {
                label {
                    htmlFor = "rounds"
                    +"Number of Rounds"
                }
                select {
                    id = "rounds"
                    listOf(6 to "6 Rounds (Quick)", 8 to "8 Rounds", 10 to "10 Rounds (Standard)", 12 to "12 Rounds (Extended)").forEach {
                        (rounds, label) ->
                        option {
                            value = rounds.toString()
                            if (rounds == settings.rounds) selected = true
                            +label
                        }
                    }
                }
            }
            div {
                label {
                    htmlFor = "password"
                    +"Password (Optional)"
                }
                input(type = InputType.password) {
                    id = "password"
                    placeholder = "Enter password..."
                    value = settings.password
                }
                div { +"Private lobby requires password to join" }
            }
        }
    }
}

private fun FlowContent.formActionsSection() {
    div {
        button {
            attributes["hx-post"] = "/games/new"
            attributes["hx-target"] = "body"
            attributes["hx-swap"] = "outerHTML"

            span { +"🚀" }
            +"Create Game"
        }
    }
}
