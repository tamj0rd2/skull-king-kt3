package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.model.GameId
import net.jqwik.api.Provide
import net.jqwik.api.domains.DomainContextBase
import net.jqwik.kotlin.api.anyForType

@Suppress("unused")
object GameArbs : DomainContextBase() {
    @Provide
    fun gameIdArb() = anyForType<GameId>()
}
