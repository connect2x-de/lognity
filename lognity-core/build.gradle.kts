@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import de.connect2x.conventions.asAAR
import de.connect2x.conventions.configureJava
import de.connect2x.conventions.defaultCompilerOptions
import de.connect2x.conventions.setProjectInfo
import de.connect2x.conventions.withAll
import de.connect2x.conventions.withJavadocJar
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.android.library)
}

configureJava(libs.versions.java)

@OptIn(ExperimentalWasmDsl::class) //
kotlin {
    defaultCompilerOptions()
    withSourcesJar()
    withJavadocJar()
    withAll()
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
                api(sharedLibs.kotlinx.io.core)
                api(sharedLibs.kotlinx.io.bytestring)
                api(projects.lognityApi)
                implementation(sharedLibs.kotlinx.coroutines.core)
                implementation(sharedLibs.kotlinx.datetime)
                implementation(libs.stately.common)
                implementation(libs.stately.collections)
            }
        }
        commonTest {
            dependencies {
                implementation(sharedLibs.kotlin.test)
            }
        }
        jvmMain {
            dependencies {
                implementation(sharedLibs.jna)
                implementation(sharedLibs.jna.platform)
            }
        }
        androidMain {
            dependencies {
                implementation(sharedLibs.jna.asProvider().asAAR())
            }
        }
        webMain {
            dependencies {
                implementation(sharedLibs.kotlin.browser)
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
                name = "Lognity Core",
                description = "Lightweight Lognity API implementation for Kotlin/Multiplatform",
                repository = "https://gitlab.com/connect2x/lognity"
            )
        }
    }
}