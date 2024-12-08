package com.tamj0rd2.skullking.adapter.web

object Main {
    private const val DEFAULT_PORT = 9000

    @JvmStatic
    fun main(
        @Suppress("unused") args: Array<String>,
    ) {
        WebServer(port = DEFAULT_PORT).start()
    }
}
