enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "lognity"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
        maven("https://central.sonatype.com/repository/maven-snapshots")
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven("https://central.sonatype.com/repository/maven-snapshots")
    }
}

include("lognity-api")
include("lognity-core")
include("lognity-slf4j")
include("lognity-java-logging")
include("lognity-ktor")
include("lognity-config")
include("lognity-example")