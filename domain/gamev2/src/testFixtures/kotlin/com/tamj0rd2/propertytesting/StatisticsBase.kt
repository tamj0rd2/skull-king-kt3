package com.tamj0rd2.propertytesting

import com.tamj0rd2.propertytesting.MyStatisticsReporter.printClassifications
import io.kotest.property.PropertyContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.full.declaredMemberProperties

data class Classifier(
    val name: String,
    val required: Boolean,
) {
    override fun toString(): String {
        if (required) return name
        return "$name (optional)"
    }
}

open class StatisticsBase {
    internal val requiredClassifiers: Set<Classifier> by lazy {
        this::class
            .declaredMemberProperties
            .map { it.call(this) as Classifier }
            .filter { it.required }
            .toSet()
    }

    protected fun required() =
        ReadOnlyProperty<Any, Classifier> { _, it ->
            Classifier(
                name = it.name,
                required = true,
            )
        }

    protected fun optional() =
        ReadOnlyProperty<Any, Classifier> { _, it ->
            Classifier(
                name = it.name,
                required = false,
            )
        }

    context(PropertyContext)
    fun classify(classifier: Classifier) = classify(classifier.name)

    context(PropertyContext)
    fun check() {
        printClassifications()

        val actualClassifiers =
            classifications()
                .keys
                .sorted()
                .toSet()

        val missedClassifications = requiredClassifiers.map { it.name } - actualClassifiers
        check(missedClassifications.isEmpty()) {
            "The following classifiers were never seen:${missedClassifications.joinToString(prefix = "\n* ")}"
        }
    }
}
