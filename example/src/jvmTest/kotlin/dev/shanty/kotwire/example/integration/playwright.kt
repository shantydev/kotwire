package dev.shanty.kotwire.example.integration

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Playwright
import io.kotest.core.spec.style.scopes.FunSpecRootScope
import io.kotest.core.test.TestScope
import org.testcontainers.containers.GenericContainer
import java.nio.file.Paths

fun FunSpecRootScope.playwrightTest(testName: String, container: GenericContainer<*>, test: suspend PlaywrightTestScope.() -> Unit) = test(testName) {
    val playwright = Playwright.create()
    val browser = playwright.chromium()
        .connect("ws://${container.host}:${container.getMappedPort(3000)}")

    val context = browser.newContext(Browser.NewContextOptions().setRecordVideoDir(Paths.get("videos/")))
    val scope = PlaywrightTestScope(context, this)

    val videosDir = Paths.get(System.getenv("VIDEO_OUTPUT_DIR"))

    try {
        test(scope)
    } finally {
        val pages = context.pages()
        pages.forEach {
            it.close()
            it.video().saveAs(videosDir.resolve("${testCase.name.name}.webm".replace(" ", "_")))
        }
        context.close()
        browser.close()
        playwright.close()
    }
}

class PlaywrightTestScope(
    val context: BrowserContext,
    testScope: TestScope,
) : TestScope by testScope
