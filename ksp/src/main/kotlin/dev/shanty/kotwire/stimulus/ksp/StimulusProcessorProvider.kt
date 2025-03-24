package dev.shanty.kotwire.stimulus.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import java.io.File

class StimulusProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val jvmOutputDir = environment.options["jvmOutputDir"]?.toString()?.let { File(it) } ?: error("jvmOutputDir not specified")
        return StimulusProcessor(
            environment.codeGenerator,
            environment.logger,
            jvmOutputDir,
        )
    }
}
