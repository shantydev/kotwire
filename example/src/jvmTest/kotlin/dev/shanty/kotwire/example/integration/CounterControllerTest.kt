package dev.shanty.kotwire.example.integration

import com.microsoft.playwright.Locator
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import dev.shanty.kotwire.example.server
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.ContainerExtension
import io.kotest.extensions.testcontainers.TestContainerSpecExtension
import io.kotest.extensions.testcontainers.perSpec
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import java.time.Duration

class CounterControllerTest : FunSpec({
    val genericContainer = GenericContainer("mcr.microsoft.com/playwright:v1.50.0-noble")
        .withExposedPorts(3000)
        .withWorkingDirectory("/home/pwuser")
        .withCommand("/bin/sh", "-c", "npx -y playwright@1.50.0 run-server --port 3000 --host 0.0.0.0")
        .withCreateContainerCmdModifier {
            it.withUser("pwuser")
        }
        .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(1)))
        .withExtraHost("hostmachine", "host-gateway")

    val container = install(TestContainerSpecExtension(genericContainer))

    val server = server().start(wait = false)

    afterSpec {
        server.stop(0, 0)
    }

    playwrightTest("counter increments and decrements", container) {
        val page = context.newPage()
        page.navigate("http://hostmachine:8080/counter")

        val locator = page.getByText("Counter demo")
        assertThat(locator).isVisible()

        val counter = page.locator("[data-counter-target=\"output\"]")
        assertThat(counter).isVisible()
        assertThat(counter).hasText("10")

        val incrementButton = page.getByText("Increment")
        assertThat(incrementButton).isVisible()

        val decrementButton = page.getByText("Decrement")
        assertThat(decrementButton).isVisible()

        incrementButton.click()
        assertThat(counter).hasText("11")

        decrementButton.click()
        assertThat(counter).hasText("10")
    }

    playwrightTest("counter cannot decrement below 0", container) {
        val page = context.newPage()
        page.navigate("http://hostmachine:8080/counter")

        val locator = page.getByText("Counter demo")
        assertThat(locator).isVisible()

        val counter = page.locator("[data-counter-target=\"output\"]")
        assertThat(counter).isVisible()
        assertThat(counter).hasText("10")

        val decrementButton = page.getByText("Decrement")
        assertThat(decrementButton).isVisible()

        decrementButton.click(
            Locator.ClickOptions()
                .setForce(true)
                .setClickCount(20)
        )

        assertThat(counter).hasText("0")
        assertThat(decrementButton).isDisabled()
    }

    playwrightTest("counter cannot increment above 100", container) {
        val page = context.newPage()
        page.navigate("http://hostmachine:8080/counter")

        val counter = page.locator("[data-counter-target=\"output\"]")
        assertThat(counter).isVisible()
        assertThat(counter).hasText("10")

        val incrementButton = page.getByText("Increment")
        assertThat(incrementButton).isVisible()

        incrementButton.click(Locator.ClickOptions()
            .setForce(true)
            .setClickCount(100)
        )

       assertThat(counter).hasText("100")
       assertThat(incrementButton).isDisabled()
    }
})
