@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import de.connect2x.conventions.configureJava
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.findKaptConfiguration

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.android.library)
    alias(libs.plugins.kotlin.kapt) // TODO: add KAPT to shared catalog
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
        @Suppress("UNUSED")
        val jvmAndAndroidMain by getting {
            dependencies {
                api(libs.slf4j.api)
                implementation(libs.autoService)
            }
        }
    }
}

val kaptConfig = findKaptConfiguration("main")!!

dependencies {
    kaptConfig(libs.autoService)
}

android {
    namespace = "$group.${rootProject.name}"
    compileSdk = sharedLibs.versions.androidCompileSDK.get().toInt()
    defaultConfig {
        minSdk = sharedLibs.versions.androidMinimalSDK.get().toInt()
    }
}

publishing {
    //setProjectInfo("Lognity SLF4j", "SLF4j integration for the Lognity logging API")
}