@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import de.connect2x.conventions.defaultCompilerOptions
import de.connect2x.conventions.setProjectInfo
import de.connect2x.conventions.withAndroidLibrary
import de.connect2x.conventions.withBrowser
import de.connect2x.conventions.withJvm
import de.connect2x.conventions.withNative
import de.connect2x.conventions.withNodeJs
import de.connect2x.conventions.withWeb
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.android.library)
    alias(sharedLibs.plugins.mavenPublish)
}

kotlin {
    defaultCompilerOptions()
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
    sourceSets {
        commonMain {
            dependencies {
                api(projects.lognityApi)
                api(sharedLibs.jetbrains.annotations)
                api(sharedLibs.kotlinx.coroutines.test)
                api(projects.lognityCore)
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
                name = "Lognity Test",
                description = "Test harness for kotlinx.test and the Lognity logging API",
                repository = "https://gitlab.com/connect2x/lognity"
            )
        }
    }
}