plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

group = "dev.shanty.kotwire"
version = project.findProperty("projectVersion") ?: "0.0.0-local"

kotlin {
    js {
        browser {
        }
    }

    jvm()

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(npm("@hotwired/stimulus", "3.2.2"))
                implementation(npm("@hotwired/hotwire-native-bridge", "1.0.0"))
                implementation(npm("@hotwired/turbo", "7.3.0"))
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val jvmMain by getting
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

    publications {
        // Automatically create publications for each target
        withType<MavenPublication> {
            pom {
                name.set("Kotwire Stimulus")
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