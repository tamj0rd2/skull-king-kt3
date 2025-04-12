package com.tamj0rd2.skullking.domain.game.values

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.values.ofResult4k
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.negativeInt
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import kotlin.test.Test

class RoundNumberTest {
    @Test
    fun `a valid round number is from 1-10`() {
        propertyTest {
            checkAll((1..10).toList().exhaustive()) { int ->
                val theResult = RoundNumber.ofResult4k(int)
                assert(theResult is Success)
            }
        }
    }

    @Test
    fun `cannot instantiate a round number less than 0`() {
        propertyTest {
            checkAll(Arb.negativeInt()) { int ->
                val theResult = RoundNumber.ofResult4k(int)
                assert(theResult is Failure)
            }
        }
    }

    @Test
    fun `cannot instantiate a round number greater than 10`() {
        propertyTest {
            checkAll(Arb.int(min = 11)) { int ->
                val theResult = RoundNumber.ofResult4k(int)
                assert(theResult is Failure)
            }
        }
    }
}
