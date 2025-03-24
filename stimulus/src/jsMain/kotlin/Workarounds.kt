import kotlinx.browser.window


// Workaround for lack of statics support https://stackoverflow.com/questions/44955888/how-to-set-a-static-property-on-a-class-in-kotlin-for-javascript
inline fun <reified T : Any> addStaticMembersTo(source: Any) {
    val c = T::class.js.asDynamic()

    val ownNames = js("Object").getOwnPropertyNames(source) as Array<String>
    val protoNames = js("Object").getOwnPropertyNames(source.asDynamic().constructor.prototype) as Array<String>

    for (name in ownNames + protoNames) {
        c[name] = source.asDynamic()[name]
    }
}

inline fun addStaticMembersTo(
    c: dynamic,
    source: Any,
) {
    val ownNames = js("Object").getOwnPropertyNames(source) as Array<String>
    val protoNames = js("Object").getOwnPropertyNames(source.asDynamic().constructor.prototype) as Array<String>

    for (name in ownNames + protoNames) {
        c[name] = source.asDynamic()[name]
    }
}
