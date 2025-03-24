import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("multiplatform") version "2.1.10"
    id("com.google.devtools.ksp") version "2.1.10-1.0.31"
    id("plugin")
    application
    id("io.kotest.multiplatform") version "5.9.1"
}

kotlin {
    js {
        browser {
            binaries.executable()
        }
    }

    jvm()

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation("dev.shanty.kotwire:stimulus:0.0.1")
                implementation("dev.shanty:kotwire:0.0.1")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation("dev.shanty.kotwire:stimulus:0.0.1")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("dev.shanty.kotwire:stimulus:0.0.1")
                implementation("dev.shanty:kotwire:0.0.1")

                val ktorVersion = "3.1.0"
                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-server-html-builder:$ktorVersion")

                implementation("ch.qos.logback:logback-classic:1.4.5")
            }

            resources.srcDir(project.layout.buildDirectory.dir("generated/jvmResources"))
        }

        val jvmTest by getting {
            dependencies {
                implementation("com.microsoft.playwright:playwright:1.50.0")
                implementation("io.kotest:kotest-runner-junit5:5.9.1")
                implementation("io.kotest:kotest-extensions-junitxml:5.9.1")
                implementation("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")
                implementation("org.testcontainers:testcontainers:1.18.3")
            }
        }
    }
}

application {
    mainClass.set("dev.shanty.kotwire.example.MainKt")
}

tasks.findByName("jvmProcessResources")?.dependsOn("copyClientJs")

tasks.register("copyClientJs") {
    val clientBuildTask = tasks.named<KotlinWebpack>("jsBrowserProductionWebpack").get()
    dependsOn(clientBuildTask)

    doLast {
        copy {
            val buildDir = clientBuildTask.outputDirectory.get().asFile
            val into = project.layout.buildDirectory.dir("generated/jvmResources/assets")
            from(buildDir)
            into(into)
        }
    }
}

project.tasks.withType<Kotlin2JsCompile> {
    compilerOptions {
        useEsClasses.set(true)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    reports {
        junitXml.required.set(false)
    }

    systemProperty("gradle.build.dir", project.buildDir)
    environment("VIDEO_OUTPUT_DIR", project.layout.buildDirectory.dir("test-results/playwright/videos").get().asFile.absolutePath)
}
