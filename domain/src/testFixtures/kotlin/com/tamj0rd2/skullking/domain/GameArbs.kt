package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.StdLibArbs.uuidArb
import com.tamj0rd2.skullking.domain.model.GameId
import net.jqwik.api.Arbitrary
import net.jqwik.api.Provide
import net.jqwik.api.domains.DomainContextBase

@Suppress("unused")
object GameArbs : DomainContextBase() {
    @Provide
    fun gameIdArb(): Arbitrary<GameId> = uuidArb().map { GameId.of(it) }.ignoreException(IllegalArgumentException::class.java)
}
