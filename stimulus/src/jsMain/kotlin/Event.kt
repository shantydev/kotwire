import dev.shanty.kotwire.stimulus.Controller
import dev.shanty.kotwire.stimulus.Event

fun Controller.dispatchEvent(event: Event<*>) {
    dispatch(event.name, event.payload)
}
