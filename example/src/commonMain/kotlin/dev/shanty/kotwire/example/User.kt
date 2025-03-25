package dev.shanty.kotwire.example

import kotlinx.serialization.Serializable
import kotlin.js.JsName

@Serializable
data class User(
    // Note that the JS name needs to be set otherwise Kotlin will mangle the name
    @JsName("name")
    val name: String
)
