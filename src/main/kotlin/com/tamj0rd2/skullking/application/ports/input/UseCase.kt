package com.tamj0rd2.skullking.application.ports.input

fun interface UseCase<I, O> {
    fun execute(input: I): O
}
