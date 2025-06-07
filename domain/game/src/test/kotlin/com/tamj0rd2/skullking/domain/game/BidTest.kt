package com.tamj0rd2.skullking.domain.game

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.negativeInt
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.assertions.isFailure
import strikt.assertions.isSuccess

class BidTest {
    @Test
    fun `a valid bid is from 0, up to and including 10`() = propertyTest {
        checkAll((0..10).toList().exhaustive()) { bidAmount ->
            expectCatching { Bid.of(bidAmount) }.isSuccess()
        }
    }

    @Test
    fun `a bid cannot be less than 0`() = propertyTest {
        checkAll(Arb.negativeInt()) { bidAmount ->
            expectCatching { Bid.of(bidAmount) }.isFailure()
        }
    }

    @Test
    fun `a bid cannot be greater than 10`() = propertyTest {
        checkAll(Arb.int(min = 11)) { bidAmount ->
            expectCatching { Bid.of(bidAmount) }.isFailure()
        }
    }
}
