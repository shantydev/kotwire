import JsonExampleController.Values
import dev.shanty.kotwire.example.User
import dev.shanty.kotwire.stimulus.Controller
import dev.shanty.kotwire.stimulus.StimulusController
import org.w3c.dom.HTMLElement

@JsExport
@StimulusController(
    targets = ["output"],
    values = Values::class,
)
class JsonExampleController(context: dynamic) : Controller(context), JsonExampleControllerTargetLifecycleHooks {
    val output by outputTarget<HTMLElement>()
    val user by userValue()

    override fun connect() {
        println("Connect ${user?.name}")
        output.innerText = user?.name ?: ""
    }

    override fun userValueChanged(value: User?, previousValue: User?) {
        output.innerText = value?.name ?: ""
    }

    object Values {
        val user: User? = null
    }
}
