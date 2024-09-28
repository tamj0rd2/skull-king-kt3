package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.GameUpdate

interface GameUpdateListener {
    fun notify(updates: List<GameUpdate>)
}
