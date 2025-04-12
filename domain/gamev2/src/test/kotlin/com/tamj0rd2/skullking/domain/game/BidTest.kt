package com.tamj0rd2.skullking.domain.game

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

class BidTest {
    @Test
    fun `a valid bid is from 0-10`() {
        propertyTest {
            checkAll((0..10).toList().exhaustive()) { int ->
                val theResult = Bid.ofResult4k(int)
                assert(theResult is Success)
            }
        }
    }

    @Test
    fun `cannot instantiate a bid less than 0`() {
        propertyTest {
            checkAll(Arb.negativeInt()) { int ->
                val theResult = Bid.ofResult4k(int)
                assert(theResult is Failure)
            }
        }
    }

    @Test
    fun `cannot instantiate a bid greater than 10`() {
        propertyTest {
            checkAll(Arb.int(min = 11)) { int ->
                val theResult = Bid.ofResult4k(int)
                assert(theResult is Failure)
            }
        }
    }
}
