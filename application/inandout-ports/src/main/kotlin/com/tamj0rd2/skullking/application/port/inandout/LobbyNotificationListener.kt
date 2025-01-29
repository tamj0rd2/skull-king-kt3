package com.tamj0rd2.skullking.application.port.inandout

fun interface LobbyNotificationListener {
    fun receive(updates: List<LobbyNotification>)

    fun receive(vararg updates: LobbyNotification) {
        require(updates.isNotEmpty()) { "must send at least 1 game update" }
        receive(updates.toList())
    }
}
