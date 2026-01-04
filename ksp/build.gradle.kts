import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    id("maven-publish")
    alias(libs.plugins.shadow)
}

group = "dev.shanty.kotwire"
version = project.findProperty("projectVersion") ?: "0.0.0-local"

dependencies {
    implementation(project(":stimulus"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.3.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.4")
    implementation("com.squareup:kotlinpoet:1.18.1")
    implementation("com.squareup:kotlinpoet-ksp:1.18.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xmulti-dollar-interpolation")
    }
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("fat")
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
        create<MavenPublication>("ksp") {
            pom {
                name.set("Kotwire KSP")
                description.set("Code Generation for Kotwire")
                url.set("https://github.com/shantydev/kotwire")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                from(components["java"])
            }
        }
    }
}