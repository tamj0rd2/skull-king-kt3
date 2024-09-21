package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.model.PlayerId
import net.jqwik.api.Provide
import net.jqwik.api.domains.DomainContextBase
import net.jqwik.kotlin.api.anyForType

@Suppress("unused")
object PlayerArbs : DomainContextBase() {
    @Provide
    fun playerIdArb() = anyForType<PlayerId>()
}
