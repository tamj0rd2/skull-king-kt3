package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import kotlinx.html.HEAD
import kotlinx.html.HTML
import kotlinx.html.head
import kotlinx.html.lang
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.styleLink
import kotlinx.html.title

internal fun HTML.common(title: String) {
    lang = "en"

    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title(title)
        styles()
        scripts()
    }
}

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
    script {
        defer = true
        src = "https://cdn.jsdelivr.net/npm/alpinejs@3.x.x/dist/cdn.min.js"
        attributes["crossorigin"] = "anonymous"
    }
}

internal fun HEAD.styles() {
    meta("color-scheme", content = "light dark")
    styleLink("https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css")
}

fun GameId.forDisplay(): String = GameId.show(this)

fun PlayerId.forDisplay() = PlayerId.show(this)
