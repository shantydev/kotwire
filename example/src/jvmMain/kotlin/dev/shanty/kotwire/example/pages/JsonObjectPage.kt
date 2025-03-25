package dev.shanty.kotwire.example.pages

import dev.shanty.kotwire.example.User
import io.ktor.server.application.ApplicationCall
import io.ktor.server.html.respondHtmlTemplate
import jsonExampleController
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.serialization.json.Json

suspend fun ApplicationCall.respondJsonObjectPage() {
    respondHtmlTemplate(BaseTemplate()) {
        content {
            h1 {
                +"Json Object Demo"
            }

            jsonExampleController {
                user = User(name = "Hello World")

                div {
                    +Json.encodeToString(this@jsonExampleController.user)
                }

                div {
                    this@jsonExampleController.outputTarget(this)
                }
            }
        }
    }
}
