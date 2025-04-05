package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
import io.kotest.common.runBlocking
import java.io.OutputStream
import java.io.PrintStream
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KVisibility
import kotlin.text.RegexOption.MULTILINE

object PropertyTesting {
    init {
        System.setProperty("kotest.assertions.collection.print.size", "10")
        io.kotest.property.PropertyTesting.shouldPrintShrinkSteps = false
        System.setOut(PrintStream(OutputStream.nullOutputStream()))
    }

    private val stackTracePartsToIgnore =
        setOf(
            PropertyTesting::class.qualifiedName!!,
            "io.kotest",
            "kotlin.coroutines",
            "kotlin.test",
        )

    private fun Throwable.rootCause(): Throwable = cause?.rootCause() ?: this

    private fun Throwable.cleanedStackTrace(): Array<StackTraceElement> =
        stackTrace.filter { element -> stackTracePartsToIgnore.none { element.className.startsWith(it) } }.toTypedArray()

    fun propertyTest(block: suspend () -> Unit) {
        try {
            // makes kotest shut up.
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
            throw AssertionError("Property failed (seed: $seed)\n\n${args.joinToString("\n")}\n${rootCause.message}", rootCause).also {
                it.stackTrace =
                    rootCause.stackTrace
            }
        }
    }

    open class ClassificationsBase {
        internal val classifiers by lazy {
            @Suppress("NO_REFLECTION_IN_CLASS_PATH")
            this::class
                .members
                .filter { it is KProperty && it.visibility == KVisibility.PUBLIC }
                .map { it.name }
                .toSet()
        }

        protected fun classification() = ReadOnlyProperty<GameClassifications, String> { _, it -> it.name }
    }
}

@Deprecated("delete this")
val somePlayers = setOf(PlayerId.random(), PlayerId.random())

@Deprecated("delete this")
fun Game.mustExecute(command: GameCommand) = execute(command).orThrow()
