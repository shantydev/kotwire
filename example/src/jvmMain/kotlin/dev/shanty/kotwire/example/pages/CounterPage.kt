package dev.shanty.kotwire.example.pages

import counterController
import io.ktor.server.application.ApplicationCall
import io.ktor.server.html.respondHtmlTemplate
import kotlinx.html.button
import kotlinx.html.h1
import kotlinx.html.span

suspend fun ApplicationCall.respondCounterPage() {
    respondHtmlTemplate(BaseTemplate()) {
        content {
            h1 {
                +"Counter Demo"
            }

            counterController {
                count = 10.0
                min = 0.0
                max = 100.0

                button {
                    this@counterController.decrementTarget(this)
                    +"Decrement"
                }

                span {
                    this@counterController.outputTarget(this)
                }

                button {
                    this@counterController.incrementTarget(this)
                    +"Increment"
                }
            }

        }
    }
}
