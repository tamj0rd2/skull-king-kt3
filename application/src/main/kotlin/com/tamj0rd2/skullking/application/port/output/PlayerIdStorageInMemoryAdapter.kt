package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.auth.SessionId

// TODO: move this to test fixtures once I have a real implementation
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
