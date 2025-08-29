package com.tamj0rd2.skullking.adapters.web

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
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
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe

data class LobbyInfo(
    val name: String,
    val status: LobbyStatus,
    val currentPlayers: Int,
    val maxPlayers: Int,
    val rounds: Int,
    val host: String,
    val created: String,
    val players: List<String>,
    val progress: String? = null,
)

enum class LobbyStatus {
    WAITING,
    PLAYING,
    FULL,
}

fun lobbyListHtml(): String =
    createHTML().html {
        lang = "en"
        head {
            meta(charset = "UTF-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
            title("Skull King - Games")
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
                    max-width: 1200px;
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

                .actions {
                    display: flex;
                    justify-content: center;
                    gap: 16px;
                    margin-bottom: 48px;
                    flex-wrap: wrap;
                }

                .btn {
                    padding: 12px 24px;
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

                .lobby-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
                    gap: 24px;
                    margin-bottom: 48px;
                }

                .lobby-card {
                    background: rgba(15, 23, 42, 0.8);
                    border: 1px solid rgba(51, 65, 85, 0.6);
                    border-radius: 16px;
                    padding: 24px;
                    backdrop-filter: blur(20px);
                    transition: all 0.3s ease;
                    position: relative;
                    overflow: hidden;
                }

                .lobby-card::before {
                    content: '';
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    height: 2px;
                    background: linear-gradient(90deg, #3b82f6, #a855f7, #06b6d4);
                    opacity: 0;
                    transition: opacity 0.3s ease;
                }

                .lobby-card:hover {
                    transform: translateY(-4px);
                    border-color: rgba(99, 102, 241, 0.5);
                    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
                }

                .lobby-card:hover::before {
                    opacity: 1;
                }

                .lobby-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 20px;
                }

                .lobby-name {
                    font-size: 1.4rem;
                    font-weight: 700;
                    color: #f8fafc;
                }

                .lobby-status {
                    padding: 6px 14px;
                    border-radius: 9999px;
                    font-size: 0.875rem;
                    font-weight: 600;
                    letter-spacing: 0.025em;
                }

                .status-waiting {
                    background: rgba(34, 197, 94, 0.2);
                    color: #22c55e;
                    border: 1px solid rgba(34, 197, 94, 0.3);
                }

                .status-playing {
                    background: rgba(251, 146, 60, 0.2);
                    color: #fb923c;
                    border: 1px solid rgba(251, 146, 60, 0.3);
                }

                .status-full {
                    background: rgba(239, 68, 68, 0.2);
                    color: #ef4444;
                    border: 1px solid rgba(239, 68, 68, 0.3);
                }

                .lobby-info {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 16px;
                    margin-bottom: 20px;
                }

                .info-item {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }

                .info-label {
                    color: #94a3b8;
                    font-size: 0.875rem;
                }

                .info-value {
                    font-weight: 600;
                    color: #f8fafc;
                    font-size: 0.875rem;
                }

                .players-list {
                    margin-bottom: 20px;
                }

                .players-title {
                    font-size: 0.875rem;
                    font-weight: 600;
                    margin-bottom: 12px;
                    color: #cbd5e1;
                }

                .player-tags {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 8px;
                }

                .player-tag {
                    background: rgba(99, 102, 241, 0.15);
                    color: #a5b4fc;
                    padding: 6px 12px;
                    border-radius: 8px;
                    font-size: 0.8rem;
                    font-weight: 500;
                    border: 1px solid rgba(99, 102, 241, 0.2);
                }

                .lobby-actions {
                    display: flex;
                    gap: 12px;
                }

                .btn-small {
                    padding: 10px 16px;
                    font-size: 0.875rem;
                    flex: 1;
                    justify-content: center;
                }

                .btn-small:disabled {
                    opacity: 0.5;
                    cursor: not-allowed;
                    transform: none !important;
                }

                @media (max-width: 768px) {
                    .title {
                        font-size: 2.5rem;
                    }

                    .lobby-grid {
                        grid-template-columns: 1fr;
                    }

                    .container {
                        padding: 16px;
                    }
                }
                """
                        .trimIndent()
                }
            }
        }
        body {
            div(classes = "container") {
                header(classes = "header") {
                    h1(classes = "title") { +"Skull King" }
                    p(classes = "subtitle") { +"Join the ultimate card battle" }
                }

                div(classes = "actions") {
                    a(href = "/prototype/create-game", classes = "btn btn-primary") {
                        span { +"➕" }
                        +"Create Lobby"
                    }
                    button(classes = "btn btn-secondary") {
                        span { +"🔄" }
                        +"Refresh"
                    }
                }

                div(classes = "lobby-grid") {
                    lobbyCard(
                        LobbyInfo(
                            name = "Competitive Arena",
                            status = LobbyStatus.WAITING,
                            currentPlayers = 4,
                            maxPlayers = 6,
                            rounds = 10,
                            host = "ProPlayer99",
                            created = "1m ago",
                            players =
                                listOf("ProPlayer99", "CardMaster", "SkullHunter", "TrickTaker"),
                        )
                    )

                    lobbyCard(
                        LobbyInfo(
                            name = "Casual Friends",
                            status = LobbyStatus.PLAYING,
                            currentPlayers = 5,
                            maxPlayers = 5,
                            rounds = 8,
                            host = "FriendlyGamer",
                            created = "",
                            players =
                                listOf(
                                    "FriendlyGamer",
                                    "ChillPlayer",
                                    "CardNewbie",
                                    "FunTimes",
                                    "RelaxedGaming",
                                ),
                            progress = "Round 2/8",
                        )
                    )

                    lobbyCard(
                        LobbyInfo(
                            name = "Quick Match",
                            status = LobbyStatus.WAITING,
                            currentPlayers = 2,
                            maxPlayers = 4,
                            rounds = 6,
                            host = "SpeedRunner",
                            created = "30s ago",
                            players = listOf("SpeedRunner", "FastCards"),
                        )
                    )
                }
            }
        }
    }

fun FlowContent.lobbyCard(lobby: LobbyInfo) {
    div(classes = "lobby-card") {
        div(classes = "lobby-header") {
            h3(classes = "lobby-name") { +lobby.name }
            span(classes = "lobby-status ${getStatusClass(lobby.status)}") {
                +getStatusText(lobby.status)
            }
        }

        div(classes = "lobby-info") {
            div(classes = "info-item") {
                span(classes = "info-label") { +"Players" }
                span(classes = "info-value") { +"${lobby.currentPlayers}/${lobby.maxPlayers}" }
            }
            div(classes = "info-item") {
                span(classes = "info-label") { +"Rounds" }
                span(classes = "info-value") { +lobby.rounds.toString() }
            }
            div(classes = "info-item") {
                span(classes = "info-label") { +"Host" }
                span(classes = "info-value") { +lobby.host }
            }
            div(classes = "info-item") {
                span(classes = "info-label") {
                    +if (lobby.progress != null) "Progress" else "Created"
                }
                span(classes = "info-value") {
                    +if (lobby.progress != null) lobby.progress else lobby.created
                }
            }
        }

        div(classes = "players-list") {
            h4(classes = "players-title") { +"Players" }
            div(classes = "player-tags") {
                lobby.players.forEach { player -> span(classes = "player-tag") { +player } }
            }
        }

        div(classes = "lobby-actions") {
            when (lobby.status) {
                LobbyStatus.PLAYING -> {
                    button(classes = "btn btn-secondary btn-small") {
                        disabled = true
                        +"Game Full"
                    }
                }

                LobbyStatus.FULL -> {
                    button(classes = "btn btn-secondary btn-small") {
                        disabled = true
                        +"Game Full"
                    }
                }

                else -> {
                    button(classes = "btn btn-primary btn-small") { +"Join Game" }
                }
            }
            button(classes = "btn btn-secondary btn-small") { +"Spectate" }
        }
    }
}

private fun getStatusClass(status: LobbyStatus): String =
    when (status) {
        LobbyStatus.WAITING -> "status-waiting"
        LobbyStatus.PLAYING -> "status-playing"
        LobbyStatus.FULL -> "status-full"
    }

private fun getStatusText(status: LobbyStatus): String =
    when (status) {
        LobbyStatus.WAITING -> "Waiting"
        LobbyStatus.PLAYING -> "Active"
        LobbyStatus.FULL -> "Full"
    }
