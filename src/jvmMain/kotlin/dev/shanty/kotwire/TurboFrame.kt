package dev.shanty.kotwire

import kotlinx.html.*

fun FlowContent.turboFrame(
    id: String,
    block: FlowContent.() -> Unit = {},
) {
    TurboFrame(attributesMapOf("id", id), consumer).visit(block)
}

open class TurboFrame(
    initialAttributes: Map<String, String>,
    override val consumer: TagConsumer<*>,
) : HTMLTag("turbo-frame", consumer, initialAttributes, null, false, false), HtmlBlockTag
