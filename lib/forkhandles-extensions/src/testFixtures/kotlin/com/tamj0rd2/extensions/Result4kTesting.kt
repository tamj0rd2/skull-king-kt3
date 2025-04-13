package com.tamj0rd2.extensions

import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.assertThrows

inline fun <reified E : Throwable> assertFailureIs(result: dev.forkhandles.result4k.Result4k<*, Throwable>) {
    assertThrows<E>("expected a failure of type ${E::class.simpleName}") { result.orThrow() }
}
