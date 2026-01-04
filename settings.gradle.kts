plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "kotwire"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
include("stimulus")
include("plugin")
include("ksp")
