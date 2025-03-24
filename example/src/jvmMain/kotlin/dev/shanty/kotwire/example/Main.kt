package dev.shanty.kotwire.example

import dev.shanty.kotwire.example.pages.respondCounterPage
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.routing.get
import io.ktor.server.routing.routing


fun main() {
    server().start(wait = true)
}

fun server(): EmbeddedServer<*, *> = embeddedServer(Netty, port = 8080) {
    routing{
        get ("/counter") {
            call.respondCounterPage()
        }

        staticResources("/assets", "assets")
    }
}
