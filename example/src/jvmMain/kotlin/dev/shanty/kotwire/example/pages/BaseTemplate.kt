package dev.shanty.kotwire.example.pages

import dev.shanty.kotwire.installHotwire
import io.ktor.server.html.Placeholder
import io.ktor.server.html.Template
import io.ktor.server.html.insert
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.script

class BaseTemplate : Template<HTML> {
    val content = Placeholder<FlowContent>()
    override fun HTML.apply() {
        head {
            installHotwire()
            script(src = "/assets/example.js") {}
            link(rel = "stylesheet", href = "/assets/main.css")
        }

        body {
            insert(content)
        }
    }
}
