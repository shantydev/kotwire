import kotlinx.browser.window

fun installTurbo() {
    window.asDynamic()["Turbo"] = Turbo
}
