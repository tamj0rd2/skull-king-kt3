package com.tamj0rd2.skullking.adapters.web

import kotlinx.html.ButtonType
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
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.select
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.textArea
import kotlinx.html.title
import kotlinx.html.unsafe

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

data class PlayerInfo(
    val name: String,
    val status: PlayerStatus,
    val isCurrentUser: Boolean = false,
)

enum class PlayerStatus {
    HOST,
    READY,
    WAITING,
}

fun createGameHtml(
    settings: LobbySettings = LobbySettings(),
    players: List<PlayerInfo> = listOf(),
): String =
    createHTML().html {
        lang = "en"
        head {
            meta(charset = "UTF-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
            title("Skull King - Create Lobby (Modern Dark)")
            style {
                unsafe {
                    +"""
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }

                    body {
                        font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #16213e 100%);
                        min-height: 100vh;
                        color: #e4e4e7;
                        position: relative;
                    }

                    body::before {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        background-image: radial-gradient(circle at 25% 25%, rgba(56, 189, 248, 0.1) 0%, transparent 50%),
                        radial-gradient(circle at 75% 75%, rgba(168, 85, 247, 0.1) 0%, transparent 50%);
                        pointer-events: none;
                    }

                    .container {
                        max-width: 900px;
                        margin: 0 auto;
                        padding: 24px;
                        position: relative;
                        z-index: 1;
                    }

                    .header {
                        text-align: center;
                        margin-bottom: 48px;
                        padding: 32px 0;
                    }

                    .title {
                        font-size: 3.5rem;
                        font-weight: 800;
                        background: linear-gradient(135deg, #38bdf8, #a855f7);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        background-clip: text;
                        margin-bottom: 12px;
                        letter-spacing: -0.02em;
                    }

                    .subtitle {
                        font-size: 1.25rem;
                        color: #94a3b8;
                        font-weight: 400;
                    }

                    .back-nav {
                        margin-bottom: 32px;
                    }

                    .back-link {
                        display: inline-flex;
                        align-items: center;
                        gap: 8px;
                        color: #38bdf8;
                        text-decoration: none;
                        font-size: 1rem;
                        font-weight: 500;
                        transition: all 0.2s ease;
                        padding: 8px 16px;
                        border-radius: 8px;
                        border: 1px solid rgba(56, 189, 248, 0.3);
                        background: rgba(56, 189, 248, 0.1);
                    }

                    .back-link:hover {
                        background: rgba(56, 189, 248, 0.2);
                        border-color: rgba(56, 189, 248, 0.5);
                        transform: translateX(-2px);
                    }

                    .form-card {
                        background: rgba(15, 23, 42, 0.8);
                        border: 1px solid rgba(51, 65, 85, 0.6);
                        border-radius: 16px;
                        padding: 32px;
                        backdrop-filter: blur(20px);
                        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
                    }

                    .form-section {
                        margin-bottom: 32px;
                    }

                    .section-title {
                        font-size: 1.5rem;
                        color: #f8fafc;
                        margin-bottom: 20px;
                        font-weight: 700;
                        display: flex;
                        align-items: center;
                        gap: 8px;
                    }

                    .form-grid {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 20px;
                        margin-bottom: 20px;
                    }

                    .form-group {
                        display: flex;
                        flex-direction: column;
                    }

                    .form-group.full-width {
                        grid-column: 1 / -1;
                    }

                    label {
                        color: #cbd5e1;
                        font-weight: 600;
                        margin-bottom: 8px;
                        font-size: 0.875rem;
                        letter-spacing: 0.025em;
                    }

                    input, select, textarea {
                        padding: 12px 16px;
                        border: 1px solid rgba(71, 85, 105, 0.5);
                        border-radius: 8px;
                        background: rgba(30, 41, 59, 0.8);
                        color: #e4e4e7;
                        font-size: 0.875rem;
                        transition: all 0.2s ease;
                        backdrop-filter: blur(10px);
                    }

                    input:focus, select:focus, textarea:focus {
                        outline: none;
                        border-color: #3b82f6;
                        background: rgba(30, 41, 59, 1);
                        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
                    }

                    input::placeholder, textarea::placeholder {
                        color: #64748b;
                    }

                    select option {
                        background: #1e293b;
                        color: #e4e4e7;
                    }

                    .checkbox-group {
                        display: flex;
                        align-items: center;
                        gap: 12px;
                        margin-bottom: 16px;
                        padding: 12px 16px;
                        background: rgba(30, 41, 59, 0.5);
                        border: 1px solid rgba(71, 85, 105, 0.3);
                        border-radius: 8px;
                        transition: all 0.2s ease;
                    }

                    .checkbox-group:hover {
                        background: rgba(30, 41, 59, 0.7);
                    }

                    input[type="checkbox"] {
                        width: 18px;
                        height: 18px;
                        margin: 0;
                        accent-color: #3b82f6;
                    }

                    .checkbox-group label {
                        margin: 0;
                        cursor: pointer;
                        color: #e4e4e7;
                    }

                    .player-list {
                        background: rgba(30, 41, 59, 0.6);
                        border: 1px solid rgba(71, 85, 105, 0.4);
                        border-radius: 12px;
                        padding: 16px;
                        min-height: 140px;
                    }

                    .player-item {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        padding: 12px 16px;
                        background: rgba(59, 130, 246, 0.1);
                        border: 1px solid rgba(59, 130, 246, 0.2);
                        border-radius: 8px;
                        margin-bottom: 8px;
                    }

                    .player-name {
                        font-weight: 600;
                        color: #f8fafc;
                    }

                    .player-status {
                        padding: 4px 12px;
                        border-radius: 12px;
                        font-size: 0.75rem;
                        font-weight: 600;
                        letter-spacing: 0.025em;
                    }

                    .status-host {
                        background: rgba(168, 85, 247, 0.2);
                        color: #c084fc;
                        border: 1px solid rgba(168, 85, 247, 0.3);
                    }

                    .status-ready {
                        background: rgba(34, 197, 94, 0.2);
                        color: #4ade80;
                        border: 1px solid rgba(34, 197, 94, 0.3);
                    }

                    .status-waiting {
                        background: rgba(251, 146, 60, 0.2);
                        color: #fb923c;
                        border: 1px solid rgba(251, 146, 60, 0.3);
                    }

                    .form-actions {
                        display: flex;
                        gap: 16px;
                        justify-content: center;
                        margin-top: 40px;
                    }

                    .btn {
                        padding: 14px 28px;
                        border: none;
                        border-radius: 12px;
                        font-size: 1rem;
                        font-weight: 600;
                        cursor: pointer;
                        transition: all 0.2s ease;
                        text-decoration: none;
                        display: inline-flex;
                        align-items: center;
                        gap: 8px;
                        backdrop-filter: blur(20px);
                    }

                    .btn-primary {
                        background: linear-gradient(135deg, #3b82f6, #1d4ed8);
                        color: white;
                        border: 1px solid rgba(59, 130, 246, 0.5);
                    }

                    .btn-primary:hover {
                        transform: translateY(-1px);
                        box-shadow: 0 10px 25px rgba(59, 130, 246, 0.4);
                    }

                    .btn-secondary {
                        background: rgba(51, 65, 85, 0.8);
                        color: #e4e4e7;
                        border: 1px solid rgba(71, 85, 105, 0.5);
                    }

                    .btn-secondary:hover {
                        background: rgba(71, 85, 105, 0.8);
                        transform: translateY(-1px);
                    }

                    .help-text {
                        font-size: 0.8rem;
                        color: #64748b;
                        margin-top: 4px;
                    }

                    .empty-state {
                        text-align: center;
                        padding: 32px 16px;
                        color: #64748b;
                        font-style: italic;
                    }

                    @media (max-width: 768px) {
                        .title {
                            font-size: 2.5rem;
                        }

                        .form-grid {
                            grid-template-columns: 1fr;
                        }

                        .form-actions {
                            flex-direction: column;
                            align-items: center;
                        }

                        .form-card {
                            padding: 24px;
                        }
                    }
                    """
                        .trimIndent()
                }
            }
        }
        body {
            div(classes = "container") {
                div(classes = "back-nav") {
                    a(href = "/prototype", classes = "back-link") { +"← Back to Lobby List" }
                }

                header(classes = "header") {
                    h1(classes = "title") { +"Create Lobby" }
                    p(classes = "subtitle") { +"Set up your game room" }
                }

                div(classes = "form-card") {
                    form {
                        gameSettingsSection(settings)
                        optionsSection(settings)
                        playersSection(players)
                        formActionsSection()
                    }
                }
            }
        }
    }

fun FlowContent.gameSettingsSection(settings: LobbySettings) {
    div(classes = "form-section") {
        h2(classes = "section-title") {
            span { +"🎮" }
            +"Game Settings"
        }
        div(classes = "form-grid") {
            div(classes = "form-group") {
                label {
                    htmlFor = "lobby-name"
                    +"Lobby Name"
                }
                input(type = InputType.text) {
                    id = "lobby-name"
                    maxLength = "50"
                    placeholder = "Enter lobby name..."
                    value = settings.name
                }
                div(classes = "help-text") { +"Choose a memorable name for your game" }
            }
            div(classes = "form-group") {
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
        div(classes = "form-grid") {
            div(classes = "form-group") {
                label {
                    htmlFor = "rounds"
                    +"Number of Rounds"
                }
                select {
                    id = "rounds"
                    listOf(
                            6 to "6 Rounds (Quick)",
                            8 to "8 Rounds",
                            10 to "10 Rounds (Standard)",
                            12 to "12 Rounds (Extended)",
                        )
                        .forEach { (rounds, label) ->
                            option {
                                value = rounds.toString()
                                if (rounds == settings.rounds) selected = true
                                +label
                            }
                        }
                }
            }
            div(classes = "form-group") {
                label {
                    htmlFor = "password"
                    +"Password (Optional)"
                }
                input(type = InputType.password) {
                    id = "password"
                    placeholder = "Enter password..."
                    value = settings.password
                }
                div(classes = "help-text") { +"Private lobby requires password to join" }
            }
        }
        div(classes = "form-group full-width") {
            label {
                htmlFor = "description"
                +"Description"
            }
            textArea {
                id = "description"
                placeholder = "Add a description or rules for your lobby..."
                rows = "3"
                +settings.description
            }
        }
    }
}

fun FlowContent.optionsSection(settings: LobbySettings) {
    div(classes = "form-section") {
        h2(classes = "section-title") {
            span { +"⚙️" }
            +"Options"
        }
        checkboxGroup("allow-spectators", "Allow spectators", settings.allowSpectators)
        checkboxGroup("ranked-mode", "Ranked match (affects rating)", settings.rankedMode)
        checkboxGroup("quick-start", "Auto-start when lobby is full", settings.quickStart)
        checkboxGroup(
            "rejoin-allowed",
            "Allow players to rejoin if disconnected",
            settings.rejoinAllowed,
        )
    }
}

fun FlowContent.checkboxGroup(id: String, labelText: String, checked: Boolean) {
    div(classes = "checkbox-group") {
        input(type = InputType.checkBox) {
            this.id = id
            if (checked) this.checked = true
        }
        label {
            htmlFor = id
            +labelText
        }
    }
}

fun FlowContent.playersSection(players: List<PlayerInfo>) {
    div(classes = "form-section") {
        h2(classes = "section-title") {
            span { +"👥" }
            +"Players"
        }
        div(classes = "player-list") {
            if (players.isNotEmpty()) {
                players.forEach { player -> playerItem(player) }
            } else {
                // Default example player
                playerItem(PlayerInfo("ProPlayer99", PlayerStatus.HOST, true))
            }

            if (players.size <= 1) {
                div(classes = "empty-state") { +"Waiting for players to join..." }
            }
        }
    }
}

fun FlowContent.playerItem(player: PlayerInfo) {
    div(classes = "player-item") {
        span(classes = "player-name") {
            +player.name
            if (player.isCurrentUser) +" (You)"
        }
        span(classes = "player-status ${getPlayerStatusClass(player.status)}") {
            +getPlayerStatusText(player.status)
        }
    }
}

fun FlowContent.formActionsSection() {
    div(classes = "form-actions") {
        button(type = ButtonType.submit, classes = "btn btn-primary") {
            span { +"🚀" }
            +"Create Lobby"
        }
        button(type = ButtonType.button, classes = "btn btn-secondary") {
            span { +"💾" }
            +"Save Draft"
        }
    }
}

private fun getPlayerStatusClass(status: PlayerStatus): String =
    when (status) {
        PlayerStatus.HOST -> "status-host"
        PlayerStatus.READY -> "status-ready"
        PlayerStatus.WAITING -> "status-waiting"
    }

private fun getPlayerStatusText(status: PlayerStatus): String =
    when (status) {
        PlayerStatus.HOST -> "Host"
        PlayerStatus.READY -> "Ready"
        PlayerStatus.WAITING -> "Waiting"
    }
