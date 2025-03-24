package dev.shanty.kotwire.example.pages

import clipboardController
import io.ktor.server.application.ApplicationCall
import io.ktor.server.html.respondHtmlTemplate
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.textInput
import toastController

suspend fun ApplicationCall.respondEventPage() {
    respondHtmlTemplate(BaseTemplate()) {
        content {
            h1 {
                +"Event Demo"
            }

            toastController {
                div(classes = "toast") {
                    this@toastController.toastTarget(this)
                }

                clipboardController {
                    textInput {
                        this@clipboardController.sourceTarget(this)
                    }
                }
            }
        }
    }
}
