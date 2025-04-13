package com.tamj0rd2.extensions

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.assertThrows

inline fun <reified E : Throwable> assertFailureIs(result: Result4k<*, Throwable>) = assertThrows<E> { result.orThrow() }

inline fun <reified E : Throwable> assertFailureIs(
    result: Result4k<*, Throwable>,
    message: String,
) = assertThrows<E>(message) { result.orThrow() }
