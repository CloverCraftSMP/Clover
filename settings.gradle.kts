pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"

    id("dev.kikugie.loom-back-compat") version "0.4.2"

    // stonecutter template says sometimes gradle doesn't work without this?
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"

}

stonecutter {
    create(rootProject) {
        // See https://stonecutter.kikugie.dev/wiki/start/#choosing-minecraft-versions
        versions("1.21.1")
        vcsVersion = "1.21.1"
    }
}

rootProject.name = "Clover"