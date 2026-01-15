@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import de.connect2x.conventions.configureJava
import de.connect2x.conventions.setProjectInfo
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.android.library)
    alias(sharedLibs.plugins.dokka)
    `maven-publish`
    signing
}

configureJava(libs.versions.java)

kotlin {
    withSourcesJar()
    jvm()
    androidTarget {
        publishLibraryVariants("debug", "release")
    }
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndAndroid") {
                withJvm()
                withAndroidTarget()
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                api(projects.lognityApi)
            }
        }
    }
}

android {
    namespace = "$group.${rootProject.name}"
    compileSdk = sharedLibs.versions.androidCompileSDK.get().toInt()
    defaultConfig {
        minSdk = sharedLibs.versions.androidMinimalSDK.get().toInt()
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            setProjectInfo(
                name = "Lognity Java Logging",
                description = "Lognity API bridge for the Java logging facade",
                repository = "https://gitlab.com/connect2x/lognity"
            )
        }
    }
}