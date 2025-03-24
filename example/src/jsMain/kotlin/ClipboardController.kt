import dev.shanty.kotwire.stimulus.Controller
import dev.shanty.kotwire.stimulus.StimulusController
import dev.shanty.kotwire.stimulus.StimulusEvent
import kotlinx.browser.window
import org.w3c.dom.HTMLInputElement

@JsExport
@StimulusController(
    targets = ["source"],
    events = [
        StimulusEvent("copy", String::class),
    ],
)
class ClipboardController : Controller {
    val source by sourceTarget<HTMLInputElement>()

    fun copy() {
        dispatchEvent(CopyEvent(source.value))
        window.navigator.clipboard.writeText(source.value)
    }
}

