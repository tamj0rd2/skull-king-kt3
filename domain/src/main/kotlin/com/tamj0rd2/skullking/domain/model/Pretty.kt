package com.tamj0rd2.skullking.domain.model

// TODO: this definitely doesn't belong in the domain.
fun Any.pretty() =
    toString().let { toString ->
        var indentLevel = 0
        val indentWidth = 4

        fun padding() = "".padStart(indentLevel * indentWidth)
        buildString {
            var ignoreSpace = false
            toString.onEach { char ->
                when (char) {
                    '(', '[', '{' -> {
                        indentLevel++
                        appendLine(char)
                        append(padding())
                    }

                    ')', ']', '}' -> {
                        indentLevel--
                        appendLine()
                        append(padding())
                        append(char)
                    }

                    ',' -> {
                        appendLine(char)
                        append(padding())
                        ignoreSpace = true
                    }
                    ' ' -> {
                        if (!ignoreSpace) append(char)
                        ignoreSpace = false
                    }

                    else -> append(char)
                }
            }
        }
    }
