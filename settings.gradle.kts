pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}


dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }

    versionCatalogs {
        create("ft") { from("dev.kikugie.fletching-table:fletching-table.catalog:0.2-SNAPSHOT") }
    }
}


plugins {
    // stonecutter template says sometimes gradle doesn't work without this?
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("dev.kikugie.loom-back-compat") version "0.4.2"
}


includeBuild("build-logic")
stonecutter {
    create(rootProject) {
        version("26.1.x", "26.1")
        versions("1.21.1")
        vcsVersion = "26.1.x"
    }
}

rootProject.name = "Clover"