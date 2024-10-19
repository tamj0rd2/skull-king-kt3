package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.auth.SessionId

interface FindPlayerIdPort {
    fun findBy(sessionId: SessionId): PlayerId?
}

interface SavePlayerIdPort {
    fun save(
        sessionId: SessionId,
        playerId: PlayerId,
    )
}

interface PlayerIdStorage :
    FindPlayerIdPort,
    SavePlayerIdPort
