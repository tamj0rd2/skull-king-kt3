package com.tamj0rd2.skullking.testsupport

fun eventually(timeoutMs: Long = 1000L, action: () -> Unit) {
    val pollIntervalMillis = 25L
    val deadline = System.currentTimeMillis() + timeoutMs
    var lastFailure: AssertionError? = null

    while (System.currentTimeMillis() < deadline) {
        try {
            action()
            return
        } catch (e: AssertionError) {
            lastFailure = e
            Thread.sleep(pollIntervalMillis)
        }
    }

    throw AssertionError("Action did not succeed within ${timeoutMs}ms. Last failure: ${lastFailure?.message}", lastFailure)
}
