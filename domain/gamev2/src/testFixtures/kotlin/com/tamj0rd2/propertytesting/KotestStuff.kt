package com.tamj0rd2.propertytesting

import com.tamj0rd2.skullking.domain.gamev2.Game
import com.tamj0rd2.skullking.domain.gamev2.GameResult
import dev.forkhandles.result4k.Success
import io.kotest.common.ExperimentalKotest
import io.kotest.common.runBlocking
import io.kotest.property.PropTestConfig
import io.kotest.property.PropertyContext
import io.kotest.property.assume
import org.opentest4j.AssertionFailedError
import java.io.OutputStream
import java.io.PrintStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.text.RegexOption.MULTILINE

object PropertyTesting {
    init {
        System.setProperty("kotest.assertions.collection.print.size", "10")
        io.kotest.property.PropertyTesting.shouldPrintShrinkSteps = false
        // makes kotest shut up.
        System.setOut(PrintStream(OutputStream.nullOutputStream()))
    }

    private val stackTracePartsToIgnore =
        setOf(
            PropertyTesting::class.qualifiedName!!,
            "io.kotest",
            "kotlin.coroutines",
            "kotlin.test",
        )

    private fun Throwable.rootCause(): Throwable {
        if (this is AssertionFailedError) return this
        return cause?.rootCause() ?: this
    }

    private fun Throwable.cleanedStackTrace(): Array<StackTraceElement> =
        stackTrace.filter { element -> stackTracePartsToIgnore.none { element.className.startsWith(it) } }.toTypedArray()

    fun propertyTest(block: suspend () -> PropertyContext) {
        try {
            runBlocking(block)
        } catch (e: AssertionError) {
            val args =
                "Arg \\d+: .*"
                    .toRegex(MULTILINE)
                    .findAll(e.message!!)
                    .mapNotNull { it.groupValues.firstOrNull() }
                    .map { it.substringBefore(" (shrunk from") }
                    .toList()

            val seed = "Repeat this test by using seed (-?\\d+)".toRegex().find(e.message!!)?.groupValues?.lastOrNull()

            val rootCause = e.rootCause().also { it.stackTrace = it.cleanedStackTrace() }
            throw AssertionError("Property failed (seed: $seed)\n\n${args.joinToString("\n")}\n\n${rootCause.message}", rootCause).also {
                it.stackTrace =
                    rootCause.stackTrace
            }
        }
    }
}

@OptIn(ExperimentalContracts::class)
fun assumeThat(value: Boolean) {
    contract {
        returns() implies value
    }
    assume(value)
}

fun GameResult.assumeSuccess(): Game {
    assumeThat(this is Success)
    return this.value
}

@ExperimentalKotest
fun setMaxDiscardPercentage(amount: Int) = PropTestConfig(maxDiscardPercentage = amount)

@ExperimentalKotest
fun setSeed(seed: Long) = PropTestConfig(seed = seed)

@ExperimentalKotest
fun PropTestConfig.withIterations(amount: Int) = copy(iterations = amount)
