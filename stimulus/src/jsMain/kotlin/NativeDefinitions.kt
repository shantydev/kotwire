@file:JsModule("@hotwired/hotwire-native-bridge")
@file:JsNonModule
import dev.shanty.kotwire.stimulus.Controller
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLElement

open external class BridgeComponent(context: dynamic) : Controller {
    val bridgeElement: BridgeElement = definedExternally

    fun send(event: String, data: dynamic, callback: (dynamic) -> Unit)
}

external class BridgeElement(element: HTMLElement) {
    val title: String
    val disabled: Boolean
    val enabled: Boolean

    fun enableForComponent(component: BridgeComponent)
    fun hasClass(className: String): Boolean
    fun attribute(name: String): String
    fun bridgeAttribute(name: String, value: String)
    fun removeBridgeAttribute(name: String)
    fun click()
}