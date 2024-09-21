package com.tamj0rd2.extensions

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success

fun <T> T.asSuccess() = Success(this)

fun <T> T.asFailure() = Failure(this)

inline fun <T, E : Throwable, reified E2 : E> Result4k<T, E>.filterOrThrow(): Result4k<T, E2> =
    when (this) {
        is Failure -> {
            if (reason is E2) {
                Failure(reason as E2)
            } else {
                throw reason
            }
        }
        is Success -> this
    }
