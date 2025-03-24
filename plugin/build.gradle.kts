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
    plugins {
        create("stimulus-plugin") {
            id = "dev.shanty.kotwire.plugin"
            implementationClass = "dev.shanty.kotwire.stimulus.gradle.StimulusPlugin"
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
                name.set("Kotwire Gradle Plugin")
                description.set("Code Generation Plugin for Kotwire")
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
