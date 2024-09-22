package com.tamj0rd2.skullking.domain

import net.jqwik.api.Arbitraries
import net.jqwik.api.Arbitrary
import net.jqwik.api.Provide
import net.jqwik.api.domains.DomainContextBase
import java.util.UUID

@Suppress("unused")
internal object StdLibArbs : DomainContextBase() {
    @Provide
    fun uuidArb(): Arbitrary<UUID> =
        Arbitraries
            .longs()
            .tuple2()
            .map { longs -> UUID(longs.get1(), longs.get2()) }
}
