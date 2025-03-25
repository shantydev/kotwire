import java.time.LocalDateTime

plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    id("maven-publish")
}

version = project.findProperty("projectVersion") ?: "0.0.0-local"
group = "dev.shanty.kotwire"

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10")
    compileOnly("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.1.10-1.0.29")
}

gradlePlugin {
    website.set("https://github.com/shantydev/kotwire")
    vcsUrl.set("https://github.com/shantydev/kotwire")

    plugins {
        create("stimulus-plugin") {
            id = "dev.shanty.kotwire.plugin"
            implementationClass = "dev.shanty.kotwire.stimulus.gradle.StimulusPlugin"
            displayName = "Kotwire Gradle Plugin"
            description = "Code Generation Plugin for Kotwire"
            tags.set(listOf("kotlin", "code-generation", "web"))
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/shantydev/kotwire")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks.register("generateBuildInfo") {
    doLast {
        val buildInfoFile = file("$buildDir/generated/src/main/kotlin/BuildInfo.kt")
        buildInfoFile.parentFile.mkdirs()
        buildInfoFile.writeText("""
            package dev.shanty.kotwire.stimulus.gradle

            object BuildInfo {
                const val BUILD_NUMBER = "$version"
                const val BUILD_TIMESTAMP = "${LocalDateTime.now()}"
            }
        """.trimIndent())
    }
}

// Modify the compile Kotlin task to include generated sources
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateBuildInfo")
}

sourceSets {
    main {
        kotlin.srcDir("$buildDir/generated/src/main/kotlin")
    }
}