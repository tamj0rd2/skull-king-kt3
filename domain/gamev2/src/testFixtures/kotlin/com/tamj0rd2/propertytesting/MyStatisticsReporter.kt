package com.tamj0rd2.propertytesting

import io.kotest.property.PropertyContext
import kotlin.math.max
import kotlin.math.roundToInt

internal object MyStatisticsReporter {
    private fun row(
        classification: Any?,
        count: Int,
        iterations: Int,
        countPad: Int,
    ): String {
        val percentage = max(((count / iterations.toDouble() * 100.0)).roundToInt(), 1)
        return "${classification.toString().padEnd(60, ' ')} ${count.toString().padStart(countPad, ' ')} ($percentage%)"
    }

    private fun stats(
        stats: Map<String, Int>,
        iterations: Int,
    ): String {
        val countPad = iterations.toString().length
        val sorted = stats.toList().sortedBy { it.first }
        return sorted.joinToString("\n") { (classification, count) -> row(classification, count, iterations, countPad) }
    }

    fun PropertyContext.printClassifications() {
        val iterations = successes()
        val message =
            """
            |Statistics: ($iterations iterations)
            |
            |${stats(classifications(), iterations)}
            """.trimMargin()
        System.err.println(message)
    }
}
