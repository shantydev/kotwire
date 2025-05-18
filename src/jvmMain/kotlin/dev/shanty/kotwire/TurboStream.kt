package dev.shanty.kotwire

import kotlinx.html.*

enum class Action {
    APPEND,
    PREPEND,
    REPLACE,
    UPDATE,
    REMOVE,
    REFRESH
}

fun Tag.turboStream(
    action: Action,
    target: String? = null,
    requestId: String? = null,
    block: FlowContent.() -> Unit = {},
) {

    val attributes = mutableMapOf(
        "action" to action.name.lowercase(),
    )

    target?.let { attributes["target"] = it }
    requestId?.let { attributes["request-id"] = it }

    TurboStream(
        attributes,
        consumer,
    ).visit {
        template {
            block()
        }
    }
}

fun TagConsumer<*>.turboStream(
    action: Action,
    target: String,
    block: FlowContent.() -> Unit = {},
) {
    TurboStream(
        mapOf(
            "action" to action.name.lowercase(),
            "target" to target,
        ),
        this,
    ).visit {
        template {
            block()
        }
    }
}

open class TurboStream(
    initialAttributes: Map<String, String>,
    override val consumer: TagConsumer<*>,
) : HTMLTag("turbo-stream", consumer, initialAttributes, null, false, false), HtmlBlockTag

fun FlowContent.turboStreamSource(src: String) {
    TurboStreamSource(
        consumer,
        mapOf(
            "src" to src,
        ),
    ).visit { }
}

open class TurboStreamSource(
    override val consumer: TagConsumer<*>,
    initialAttributes: Map<String, String>,
) : HTMLTag("turbo-stream-source", consumer, initialAttributes, null, false, true), HtmlBlockTag

fun Tag.template(block: Template.() -> Unit = {}) {
    Template(consumer).visit(block)
}

open class Template(
    override val consumer: TagConsumer<*>,
) : HTMLTag("template", consumer, emptyMap(), null, false, false), HtmlBlockTag

//suspend fun ApplicationCall.respondTurboStream(
//    action: Action,
//    target: String,
//    html: FlowContent.() -> Unit,
//) {
//    respondText(
//        contentType = TurboStreamContentType,
//        status = HttpStatusCode.OK,
//    ) {
//        buildString {
//            appendHTML().html {
//                body {
//                    turboStream(action, target, html)
//                }
//            }
//        }
//    }
//}
//
//val TurboStreamContentType = ContentType("text", "vnd.turbo-stream.html")
