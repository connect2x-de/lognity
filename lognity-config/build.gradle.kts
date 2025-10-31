import net.folivo.lognity.gradle.setProjectInfo
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}

@OptIn(ExperimentalWasmDsl::class) //
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
    jvmToolchain(libs.versions.java.get().toInt())
    withSourcesJar()
    mingwX64()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    androidNativeX64()
    androidNativeArm64()
    androidNativeArm32()
    jvm()
    androidTarget {
        publishLibraryVariants("debug", "release")
    }
    js {
        browser {
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
    }
    applyDefaultHierarchyTemplate()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.lognityApi)
                implementation(projects.lognityCore)
                api(libs.kotlinx.io.bytestring)
                api(libs.kotlinx.io.core)
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val jvmAndAndroidMain by creating { dependsOn(commonMain) }
        val jvmMain by getting { dependsOn(jvmAndAndroidMain) }
        val androidMain by getting { dependsOn(jvmAndAndroidMain) }
    }
}

android {
    namespace = "$group.${rootProject.name}"
    compileSdk = libs.versions.androidCompileSDK.get().toInt()
    defaultConfig {
        minSdk = libs.versions.androidMinimalSDK.get().toInt()
    }
}

publishing {
    setProjectInfo("Lognity Config", "Lightweight logging configuration for Kotlin/Multiplatform")
}