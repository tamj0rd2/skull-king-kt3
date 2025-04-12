package com.tamj0rd2.extensions

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map

fun <T> T.asSuccess() = Success(this)

fun <T> T.asFailure() = Failure(this)

fun <T, E, LI> Result4k<T, E>.fold(
    items: List<LI>,
    block: T.(LI) -> Result4k<Unit, E>,
): Result4k<T, E> =
    items.fold(this) { result, item ->
        result.flatMap { actual -> actual.run { block(item) }.map { actual } }
    }
