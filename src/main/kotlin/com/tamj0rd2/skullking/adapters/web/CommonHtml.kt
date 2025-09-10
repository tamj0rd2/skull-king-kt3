package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import kotlinx.html.HEAD
import kotlinx.html.script
import kotlinx.html.styleLink

internal fun HEAD.scripts() {
    script {
        src = "https://unpkg.com/htmx.org@2.0.6"
        attributes["crossorigin"] = "anonymous"
    }
    script {
        src = "https://unpkg.com/htmx-ext-ws@2.0.2"
        attributes["crossorigin"] = "anonymous"
    }
    script {
        attributes["src"] = "https://unpkg.com/htmx-ext-ws@2.0.2"
        attributes["crossorigin"] = "anonymous"
    }
}

internal fun HEAD.styles() {
    styleLink("/css/common.css")
    styleLink("/css/list-games.css")
    styleLink("/css/create-game.css")
    styleLink("/css/game.css")
}

fun GameId.forDisplay(): String = GameId.show(this)

fun PlayerId.forDisplay() = PlayerId.show(this)
