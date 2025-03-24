import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val bridgeJson = Json {
    ignoreUnknownKeys = true
}

inline fun <reified T, reified R> BridgeComponent.send(event: String, data: T, noinline callback: (R) -> Unit) {
    // Hack it and JSON encode with KotlinX and then decode with JS
    val jsonData = bridgeJson.encodeToString(data)
    val rawObject = JSON.parse<dynamic>(jsonData)
    this.send(event, rawObject) {
        val rawJson = JSON.stringify(it.data)
        callback(bridgeJson.decodeFromString(rawJson))
    }
}
