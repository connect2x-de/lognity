rootProject.name = "lognity"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven("https://gitlab.com/api/v4/projects/68438621/packages/maven") // c2x Conventions
    }
}

@Suppress("UnstableApiUsage") //
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven("https://gitlab.com/api/v4/projects/68438621/packages/maven") // c2x Conventions
    }
}

plugins {
    id("de.connect2x.conventions.c2x-settings-plugin") version "20260124.012513"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("lognity-api")
include("lognity-core")
include("lognity-core-config")
include("lognity-slf4j")
include("lognity-java-logging")
include("lognity-ktor")
include("lognity-config")
include("lognity-example")