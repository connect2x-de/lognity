import de.connect2x.conventions.configureJava
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.android.library)
    alias(sharedLibs.plugins.kotlin.serialization)
    `maven-publish`
    signing
}

configureJava(libs.versions.java)

@OptIn(ExperimentalWasmDsl::class) //
kotlin {
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
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.io.core)
                api(libs.kotlinx.io.bytestring)
                implementation(sharedLibs.kotlinx.coroutines.core)
                implementation(sharedLibs.kotlinx.datetime)
                implementation(libs.stately.common)
                implementation(libs.stately.collections)
                implementation(sharedLibs.kotlinx.serialization.core)
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
    //setProjectInfo("Lognity API", "Lightweight logging API for Kotlin/Multiplatform")
}