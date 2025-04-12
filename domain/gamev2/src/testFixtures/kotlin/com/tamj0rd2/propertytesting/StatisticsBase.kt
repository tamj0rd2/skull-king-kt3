package com.tamj0rd2.propertytesting

import io.kotest.property.PropertyContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.full.declaredMemberProperties

data class Classifier(
    val name: String,
    val required: Boolean = true,
) {
    override fun toString(): String {
        if (required) return name
        return "$name (optional)"
    }
}

abstract class StatisticsBase<T> {
    internal open val requiredClassifiers: Set<Classifier> by lazy {
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
    fun classify(data: T) {
        classify(classifyData(data).name)
    }

    protected abstract fun classifyData(data: T): Classifier

    context(PropertyContext)
    fun check() {
        val actualClassifiers =
            classifications()
                .keys
                .sorted()
                .toSet()

        val missedClassifications = requiredClassifiers.map { it.name } - actualClassifiers
        check(missedClassifications.isEmpty()) {
            val missedClassificationsText = missedClassifications.joinToString(prefix = "\n", separator = "\n") { "* $it" }
            "The following classifiers were never seen:$missedClassificationsText"
        }
    }
}

object NoStats : StatisticsBase<Nothing>() {
    override fun classifyData(data: Nothing): Classifier {
        TODO("Not yet implemented")
    }
}
