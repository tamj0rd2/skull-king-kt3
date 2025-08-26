package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.adapters.inmemory.inMemory
import com.tamj0rd2.skullking.application.OutputPorts

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val port = 8124
        println("Starting server on port $port")
        WebServer(outputPorts = OutputPorts.inMemory(), port = port).start()
    }
}
