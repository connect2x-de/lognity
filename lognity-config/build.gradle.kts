@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import de.connect2x.conventions.configureJava
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.kotlin.serialization)
    alias(sharedLibs.plugins.android.library)
    `maven-publish`
    signing
}

configureJava(libs.versions.java)

@OptIn(ExperimentalWasmDsl::class) //
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
    withSourcesJar()
    mingwX64()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    androidNativeX64()
    androidNativeArm64()
    androidNativeArm32()
    androidNativeX86()
    jvm()
    androidTarget {
        publishLibraryVariants("debug", "release")
    }
    js {
        compilerOptions {
            sourceMap = true
            sourceMapEmbedSources = JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS
        }
        browser {
            useCommonJs()
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
        nodejs {
            useCommonJs()
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
    wasmJs {
        browser {
            useEsModules()
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
        nodejs {
            useEsModules()
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndAndroid") {
                withJvm()
                withAndroidTarget()
            }
            group("nonWeb") {
                withJvm()
                withAndroidTarget()
                withNative()
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                api(projects.lognityApi)
                api(libs.kotlinx.io.bytestring)
                api(libs.kotlinx.io.core)
                implementation(sharedLibs.kotlinx.serialization.core)
                implementation(sharedLibs.kotlinx.serialization.json)
                implementation(sharedLibs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(sharedLibs.kotlin.test)
            }
        }
        webMain {
            dependencies {
                implementation(libs.kotlinx.browser)
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
    //setProjectInfo("Lognity Config", "Lightweight logging configuration for Kotlin/Multiplatform")
}