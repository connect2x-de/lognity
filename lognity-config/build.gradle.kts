@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import de.connect2x.conventions.configureJava
import de.connect2x.conventions.setProjectInfo
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.kotlin.serialization)
    alias(sharedLibs.plugins.android.library)
    alias(sharedLibs.plugins.dokka)
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
        useCommonJs()
        compilerOptions {
            sourceMap = true
            sourceMapEmbedSources = JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS
        }
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
        nodejs {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
        nodejs {
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
                api(sharedLibs.kotlinx.io.bytestring)
                api(sharedLibs.kotlinx.io.core)
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
                name = "Lognity Config",
                description = "Extensible logging configuration subsystem for Kotlin/Multiplatform",
                repository = "https://gitlab.com/connect2x/lognity"
            )
        }
    }
}