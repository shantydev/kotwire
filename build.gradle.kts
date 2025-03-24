plugins {
    kotlin("multiplatform") version "2.1.10"
    id("maven-publish")
}

group = "dev.shanty"
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
                implementation(npm("@hotwired/turbo", "7.3.0"))
            }
        }

        val jvmMain by getting {
            dependencies {
                api(libs.kotlinx.html)
            }
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

    publications {
        // Automatically create publications for each target
        withType<MavenPublication> {
            pom {
                name.set("Kotwire")
                description.set("Kotlin Bindings and Utilities for Hotwire")
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
