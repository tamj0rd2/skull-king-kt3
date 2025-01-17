package com.tamj0rd2.skullking.domain.game

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.assertions.isFailure
import strikt.assertions.isSuccess

class RoundNumberTest {
    @Test
    fun `a valid round number is from 1, up to and including 10`() =
        propertyTest {
            checkAll((1..10).toList().exhaustive()) {
                expectCatching { RoundNumber.of(it) }.isSuccess()
            }
        }

    @Test
    fun `the round number cannot be less than 1`() =
        propertyTest {
            checkAll(Arb.int(max = 0)) {
                expectCatching { RoundNumber.of(it) }.isFailure()
            }
        }

    @Test
    fun `the round number cannot be greater than 10`() =
        propertyTest {
            checkAll(Arb.int(min = 11)) {
                expectCatching { RoundNumber.of(it) }.isFailure()
            }
        }
}
