package dev.shanty.kotwire.stimulus.gradle

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import java.io.File

class StimulusPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.afterEvaluate {
            if (!project.plugins.hasPlugin("com.google.devtools.ksp")) {
                project.plugins.apply("com.google.devtools.ksp")
            }

            project.dependencies.add("kspJs", "dev.shanty.kotwire:ksp:${BuildInfo.BUILD_NUMBER}")

            project.tasks.named("compileKotlinJvm").get().apply {
                dependsOn("kspKotlinJs")
            }

            project.extensions.findByType(KspExtension::class.java)?.apply {
                arg("jvmOutputDir", "${project.layout.buildDirectory.get()}/generated/ksp/jvm")
            }

            project.kotlinExtension.apply {
                sourceSets.getByName("jvmMain").kotlin.srcDir("${project.layout.buildDirectory.get()}/generated/ksp/jvm")
            }
        }
    }
}
