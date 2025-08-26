package com.tamj0rd2.skullking.application.ports.input

fun interface UseCase<in I : Any, out O : Any> {
    fun execute(input: I): O
}
