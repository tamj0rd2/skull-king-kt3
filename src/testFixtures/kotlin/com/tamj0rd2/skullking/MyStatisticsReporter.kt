package com.tamj0rd2.skullking

import io.kotest.common.ExperimentalKotest
import io.kotest.common.TestNameContextElement
import io.kotest.common.runBlocking
import io.kotest.property.LabelOrder
import io.kotest.property.PropertyContext
import io.kotest.property.PropertyTesting
import io.kotest.property.statistics.Label
import java.io.PrintStream
import kotlin.coroutines.coroutineContext
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalKotest::class)
class MyStatisticsReporter(private val stream: PrintStream) {
    fun print(context: PropertyContext) = runBlocking {
        val iterations = context.attempts()

        val statistics = context.statistics()
        if (statistics.isEmpty()) {
            stream.println("No statistics recorded.")
            return@runBlocking
        }

        statistics.forEach { (label, stats) ->
            stream.println()
            stream.println(header(iterations = iterations, label = label))
            stream.println()
            stream.println(stats(stats = stats, iterations = iterations))
        }
    }

    private suspend fun header(iterations: Int, label: Label?): String {
        val testName = coroutineContext[TestNameContextElement]?.testName
        val prefix = if (testName == null) "" else "[$testName]"
        val suffix = if (label == null) "" else "[${label.value}]"
        return "Statistics: $prefix ($iterations iterations) $suffix"
    }

    private fun stats(stats: Map<Any?, Int>, iterations: Int): String {
        val sorted =
            when (PropertyTesting.labelOrder) {
                LabelOrder.Quantity -> stats.toList().sortedByDescending { it.second }
                LabelOrder.Lexicographic -> stats.toList().sortedBy { it.first.toString() }
            }

        return sorted.joinToString("\n") { (classification, count) ->
            val countPad = iterations.toString().length
            val percentage = max(((count / iterations.toDouble() * 100.0)).roundToInt(), 1)
            "${classification.toString().padEnd(60, ' ')} ${count.toString().padStart(countPad, ' ')} ($percentage%)"
        }
    }
}
