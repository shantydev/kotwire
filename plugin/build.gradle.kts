plugins {
    `java-gradle-plugin`
    kotlin("jvm")
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10")
    compileOnly("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.1.10-1.0.29")
}

gradlePlugin {
    plugins {
        create("stimulus-plugin") {
            id = "dev.shanty.kotwire.stimulus"
            implementationClass = "dev.shanty.kotwire.stimulus.gradle.StimulusPlugin"
        }
    }
}