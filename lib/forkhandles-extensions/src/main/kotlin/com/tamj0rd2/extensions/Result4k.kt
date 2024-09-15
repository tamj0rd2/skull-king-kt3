package com.tamj0rd2.extensions

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success

fun <T> T.asSuccess() = Success(this)

fun <T> T.asFailure() = Failure(this)
