plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

group = "dev.shanty.kotwire"
version = "0.0.1"

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
