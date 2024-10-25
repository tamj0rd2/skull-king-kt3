package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.output.PlayerIdStorage
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.PlayerId

class PlayerIdStorageInMemoryAdapter : PlayerIdStorage {
    private val playerIds = mutableMapOf<SessionId, PlayerId>()

    override fun findBy(sessionId: SessionId): PlayerId? = playerIds[sessionId]

    override fun save(
        sessionId: SessionId,
        playerId: PlayerId,
    ) {
        playerIds[sessionId] = playerId
    }
}
