package com.tamj0rd2.skullking.adapters.web

import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.main
import kotlinx.html.stream.createHTML
import kotlinx.html.title
import kotlinx.html.ul
import org.http4k.core.Uri

fun viewGameHtml(joinGameUri: Uri): String =
    createHTML().html {
        head {
            scripts()
            title { +"Skull King - Game" }
        }

        body {
            main {
                attributes["id"] = "game"
                attributes["hx-ext"] = "ws"
                attributes["ws-connect"] = joinGameUri.toString()

                h1 { +"Game" }

                h2 { +"Players" }
                ul { id = "players" }
            }
        }
    }
