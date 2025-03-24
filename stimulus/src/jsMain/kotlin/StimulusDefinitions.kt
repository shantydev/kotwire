@file:JsModule("@hotwired/stimulus")
@file:JsNonModule

package dev.shanty.kotwire.stimulus

import org.w3c.dom.HTMLElement

external class Application {
    var debug: Boolean = definedExternally

    fun register(
        name: String,
        controllerConstructor: dynamic,
    ): Boolean

    companion object {
        fun start(): Application
    }
}

open external class Controller(context: dynamic) {
    val element: HTMLElement = definedExternally
    val application: Application = definedExternally
    val identifier: String = definedExternally

    open fun initialize()

    open fun connect()

    open fun disconnect()

    fun dispatch(
        eventName: String,
        detail: Any? = definedExternally,
    )
}
