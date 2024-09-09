package com.tamj0rd2.skullking.testhelpers

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Await {
    private val latch = CountDownLatch(1)

    fun resume() = latch.countDown()

    companion object {
        operator fun <T> invoke(
            duration: Duration = 1.seconds,
            block: Await.() -> T,
        ): T {
            val wait = Await()
            val result = wait.block()
            wait.latch.await(duration.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            return result
        }
    }
}
