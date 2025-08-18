package com.tamj0rd2.skullking.testsupport

fun eventually(action: () -> Unit) {
    val timeoutMillis = 1_000L
    val pollIntervalMillis = 25L
    val deadline = System.currentTimeMillis() + timeoutMillis
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

    throw AssertionError(
        "Action did not succeed within ${timeoutMillis}ms. Last failure: ${lastFailure?.message}",
        lastFailure,
    )
}
