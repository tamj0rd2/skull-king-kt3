package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.port.output.PlayerIdStorage
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.auth.SessionId

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
