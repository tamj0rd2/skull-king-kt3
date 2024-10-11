package com.tamj0rd2.testhelpers

import java.time.Instant

fun <T> eventually(block: () -> T): T {
    val stopAt = Instant.now().plusMillis(200)
    var lastError: AssertionError?
    do {
        try {
            return block()
        } catch (e: AssertionError) {
            lastError = e
        }
    } while (stopAt > Instant.now())

    throw checkNotNull(lastError)
}
