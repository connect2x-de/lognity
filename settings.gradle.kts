enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "lognity"

buildCache {
    val buildCacheUrl = System.getenv("GRADLE_BUILD_CACHE_URL")
    local {
        isEnabled = buildCacheUrl == null
        directory = File(rootDir, ".gradle").resolve("build-cache")
    }
    remote<HttpBuildCache> {
        isEnabled = buildCacheUrl != null
        if (buildCacheUrl != null) {
            url = uri(buildCacheUrl)
            isPush = true
            credentials {
                username = System.getenv("GRADLE_BUILD_CACHE_USERNAME")
                password = System.getenv("GRADLE_BUILD_CACHE_PASSWORD")
            }
        }
    }
}

pluginManagement {
    repositories {
        System.getenv("GRADLE_DEPENDENCY_CACHE_URL")?.let { cacheUrl ->
            maven {
                url = uri(cacheUrl)
                authentication {
                    credentials {
                        username = System.getenv("GRADLE_DEPENDENCY_CACHE_USERNAME")
                        password = System.getenv("GRADLE_DEPENDENCY_CACHE_PASSWORD")
                    }
                }
            }
        }
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
        maven("https://central.sonatype.com/repository/maven-snapshots")
        maven("https://gitlab.com/api/v4/projects/68438621/packages/maven") // c2x Conventions
    }
}

@Suppress("UnstableApiUsage") //
dependencyResolutionManagement {
    repositories {
        System.getenv("GRADLE_DEPENDENCY_CACHE_URL")?.let { cacheUrl ->
            maven {
                url = uri(cacheUrl)
                authentication {
                    credentials {
                        username = System.getenv("GRADLE_DEPENDENCY_CACHE_USERNAME")
                        password = System.getenv("GRADLE_DEPENDENCY_CACHE_PASSWORD")
                    }
                }
            }
        }
        google()
        mavenCentral()
        mavenLocal()
        maven("https://central.sonatype.com/repository/maven-snapshots")
        maven("https://gitlab.com/api/v4/projects/68438621/packages/maven") // c2x Conventions
    }

    versionCatalogs {
        create("sharedLibs") {
            from("de.connect2x.conventions:c2x-shared-catalog:20260108.083825")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include("lognity-api")
include("lognity-core")
include("lognity-core-config")
include("lognity-slf4j")
include("lognity-java-logging")
include("lognity-ktor")
include("lognity-config")
include("lognity-example")