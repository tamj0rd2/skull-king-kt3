package com.tamj0rd2.skullking.adapters.web

import java.io.StringWriter
import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer
import kotlinx.html.stream.appendHTML

internal class PartialBlock private constructor() : FlowContent {
    private val writer = StringWriter()
    override val consumer: TagConsumer<*> = writer.appendHTML()

    override val attributes: MutableMap<String, String> = mutableMapOf()
    override val attributesEntries: Collection<Map.Entry<String, String>> = emptyList()
    override val emptyTag: Boolean = true
    override val inlineTag: Boolean = true
    override val namespace: String? = null
    override val tagName: String = ""

    fun toHtml(): String = writer.toString()

    companion object {
        fun partial(block: FlowContent.() -> Unit): String = PartialBlock().apply(block).toHtml()
    }
}
