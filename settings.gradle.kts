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
include("stimulus:gradle")
findProject(":stimulus:gradle")?.name = "gradle"
include("stimulus:ksp")
findProject(":stimulus:ksp")?.name = "ksp"
include("plugin")
include("ksp")
