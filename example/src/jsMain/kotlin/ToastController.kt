import dev.shanty.kotwire.stimulus.Controller
import dev.shanty.kotwire.stimulus.StimulusController
import kotlinx.browser.window
import org.w3c.dom.HTMLElement

@JsExport
@StimulusController(
    values = ToastController.Values::class,
    targets = ["toast"],
)
class ToastController(context: dynamic) : Controller(context) {
    val toast by toastTarget<HTMLElement>()
    val prefix by prefixValue()
    val duration by durationValue()

    fun show(message: String) {
        toast.textContent = "$prefix$message"
        toast.classList.add("show")
        window.setTimeout({
            toast.classList.remove("show")
        }, duration.toInt())
    }

    object Values {
        val prefix: String = ""
        val duration: Int = 1000
    }
}
