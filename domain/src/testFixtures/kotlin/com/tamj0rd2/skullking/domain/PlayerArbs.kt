package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.model.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.PlayerId
import net.jqwik.api.Provide
import net.jqwik.api.domains.DomainContextBase
import net.jqwik.kotlin.api.anyForType

@Suppress("unused")
object PlayerArbs : DomainContextBase() {
    @Provide
    fun playerIdArb() = anyForType<PlayerId>()

    @Provide
    fun playerIdsArb() = playerIdArb().list().uniqueElements().ofMaxSize(MAXIMUM_PLAYER_COUNT)
}
