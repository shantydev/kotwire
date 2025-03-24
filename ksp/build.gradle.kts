import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "dev.shanty.kotwire"
version = "0.0.1"

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