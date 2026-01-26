@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import de.connect2x.conventions.configureJava
import de.connect2x.conventions.defaultCompilerOptions
import de.connect2x.conventions.setProjectInfo
import de.connect2x.conventions.withAndroidLibrary
import de.connect2x.conventions.withBrowser
import de.connect2x.conventions.withJvm
import de.connect2x.conventions.withNative
import de.connect2x.conventions.withNodeJs
import de.connect2x.conventions.withWeb
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.android.library)
    alias(sharedLibs.plugins.mavenPublish)
}

configureJava(sharedLibs.versions.targetJvm)

@OptIn(ExperimentalWasmDsl::class) //
kotlin {
    withSourcesJar()
    withAndroidLibrary()
    withJvm()
    withNative()
    withWeb {
        withBrowser()
        withNodeJs()
    }
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndAndroid") {
                withJvm()
                withAndroidTarget()
            }
        }
    }
    defaultCompilerOptions()
    sourceSets {
        commonMain {
            dependencies {
                api(projects.lognityApi)
                api(sharedLibs.ktor.client.core)
                api(sharedLibs.ktor.client.logging)
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            setProjectInfo(
                name = "Lognity Ktor Client",
                description = "Lognity API bridge for Ktor Client",
                repository = "https://gitlab.com/connect2x/lognity"
            )
        }
    }
}