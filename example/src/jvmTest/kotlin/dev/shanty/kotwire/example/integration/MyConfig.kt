package dev.shanty.kotwire.example.integration

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.junitxml.JunitXmlReporter

class MyConfig : AbstractProjectConfig() {
  override val extensions: List<Extension> = listOf(
    JunitXmlReporter(
      includeContainers = false, // don't write out status for all tests
      useTestPathAsName = true, // use the full test path (ie, includes parent test names)
      outputDir = "test-results/jvmTest" // include to set output dir for maven
    )
  )
}