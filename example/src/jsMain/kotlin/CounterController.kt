import CounterController.Values
import dev.shanty.kotwire.stimulus.Controller
import dev.shanty.kotwire.stimulus.StimulusController
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement

@JsExport
@StimulusController(
    targets = ["increment", "decrement", "output"],
    values = Values::class,
)
class CounterController(context: dynamic) : Controller(context), CounterControllerTargetLifecycleHooks {
    var count: Double by countValue()

    val incrementButton by incrementTarget<HTMLButtonElement>()
    val decrementButton by decrementTarget<HTMLButtonElement>()
    val output by outputTarget<HTMLElement>()

    val min by minValue()
    val max by maxValue()

    override fun connect() {
        output.textContent = count.toString()

        incrementButton.onclick = {
            count++
        }

        decrementButton.onclick = {
            count--
        }
    }

    override fun countValueChanged(value: Double, previousValue: Double) {
        if(hasMaxValue() && value > max) {
            count = max
        }

        if(hasMinValue() && value < min) {
            count = min
        }

        if(hasMaxValue() && count >= max) {
            incrementButton.disabled = true
        } else {
            incrementButton.disabled = false
        }

        if(hasMinValue() && count <= min) {
            decrementButton.disabled = true
        } else {
            decrementButton.disabled = false
        }

        output.textContent = value.toString()
    }

    override fun disconnect() {
        incrementButton.onclick = null
        decrementButton.onclick = null
    }

    object Values {
        val count: Int = 0
        val min: Int? = null
        val max: Int? = null
    }
}
