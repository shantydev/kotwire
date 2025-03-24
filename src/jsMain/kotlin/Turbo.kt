import org.w3c.dom.HTMLElement

@JsModule("@hotwired/turbo")
@JsNonModule
external object Turbo {
    fun renderStreamMessage(
        html: String,
        target: HTMLElement = definedExternally,
    )

    fun visit(location: String)
}