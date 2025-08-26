package com.tamj0rd2.skullking.adapters.web

import kotlinx.html.HEAD
import kotlinx.html.script

internal fun HEAD.scripts() {
    script {
        attributes["src"] = "https://unpkg.com/htmx.org@2.0.6"
        attributes["crossorigin"] = "anonymous"
    }
    script {
        attributes["src"] = "https://unpkg.com/htmx-ext-ws@2.0.2"
        attributes["crossorigin"] = "anonymous"
    }
}
