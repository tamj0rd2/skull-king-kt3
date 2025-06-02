package com.tamj0rd2.skullking.domain.gamev3

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import io.kotest.common.ExperimentalKotest
import io.kotest.common.runBlocking
import io.kotest.property.Constraints
import io.kotest.property.PropTestConfig
import io.kotest.property.PropertyContext
import io.kotest.property.assume
import io.kotest.property.or
import io.kotest.property.statistics.Label
import org.junit.jupiter.api.fail
import org.opentest4j.AssertionFailedError
import java.io.OutputStream
import java.io.PrintStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.text.RegexOption.MULTILINE
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalKotest::class)
object PropertyTesting {
    init {
        System.setProperty("kotest.assertions.collection.print.size", "10")
        io.kotest.property.PropertyTesting.shouldPrintShrinkSteps = false
        // makes kotest shut up.
        System.setOut(PrintStream(OutputStream.nullOutputStream()))
    }

    private const val MIN_ATTEMPT_COUNT = 1000

    @OptIn(ExperimentalKotest::class)
    val propTestConfig get() =
        PropTestConfig(
            maxDiscardPercentage = 99,
            // stops when the attempt limit is met and the duration is reached.
            constraints = Constraints { it.attempts() < MIN_ATTEMPT_COUNT }.or(Constraints.duration(2.seconds)),
        )

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

    fun PropertyContext.printStatistics() = apply { MyStatisticsReporter(System.err).print(this) }

    fun propertyTest(block: suspend () -> PropertyContext): PropertyContext {
        try {
            return runBlocking(block)
                .also { it.printStatistics() }
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
                it.stackTrace = rootCause.stackTrace
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

    fun <T> Result4k<T, *>.assumeWasSuccessful(): T {
        assumeThat(this is Success)
        return value
    }

    @OptIn(ExperimentalKotest::class)
    fun PropertyContext.checkCoverageExists(
        label: String? = null,
        expectedClassifications: Set<Any?>,
    ) = apply {
        val stats = statistics().getOrDefault(label?.let(::Label), emptyMap())
        val nonExistentClassifications = expectedClassifications - stats.keys

        if (nonExistentClassifications.isNotEmpty()) {
            fail(
                """
                Expected classifications to be collected at least once:
                Seen: ${stats.keys.joinToString(", ")}
                Never seen: ${nonExistentClassifications.joinToString(", ")}
                """.trimIndent(),
            )
        }
    }
}
