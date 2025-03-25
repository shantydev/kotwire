import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    id("maven-publish")
}

group = "dev.shanty.kotwire"
version = project.findProperty("projectVersion") ?: "0.0.0-local"

dependencies {
    implementation(project(":stimulus"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.10")
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.10-1.0.29")
    implementation("com.squareup:kotlinpoet:1.18.1")
    implementation("com.squareup:kotlinpoet-ksp:1.18.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xmulti-dollar-interpolation"
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

    publications {
        // Automatically create publications for each target
        withType<MavenPublication> {
            pom {
                name.set("Kotwire KSP")
                description.set("Kotlin Bindings and Utilities for Stimulus")
                url.set("https://github.com/shantydev/kotwire")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }
}