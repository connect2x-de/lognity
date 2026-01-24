@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import de.connect2x.conventions.configureJava
import de.connect2x.conventions.defaultCompilerOptions
import de.connect2x.conventions.setProjectInfo
import de.connect2x.conventions.withAndroidLibrary
import de.connect2x.conventions.withJs
import de.connect2x.conventions.withJvm
import de.connect2x.conventions.withNative
import de.connect2x.conventions.withWasm
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.kotlin.serialization)
    alias(sharedLibs.plugins.android.library)
    alias(sharedLibs.plugins.mavenPublish)
}

configureJava(libs.versions.java)

@OptIn(ExperimentalWasmDsl::class) //
kotlin {
    defaultCompilerOptions()
    withSourcesJar()
    withAndroidLibrary()
    withJvm()
    withNative()
    withJs {
        browser()
        nodejs()
    }
    withWasm {
        browser()
        nodejs()
    }
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain {
            dependencies {
                api(projects.lognityConfig)
                api(projects.lognityCore)
                implementation(sharedLibs.kotlinx.serialization.core)
                implementation(sharedLibs.kotlinx.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(sharedLibs.kotlin.test)
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            setProjectInfo(
                name = "Lognity Core Config",
                description = "Lognity Config extension for Lognity Core",
                repository = "https://gitlab.com/connect2x/lognity"
            )
        }
    }
}