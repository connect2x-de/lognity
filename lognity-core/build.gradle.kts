/*
 * Copyright 2025 Trixnity
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import dev.karmakrafts.conventions.configureJava
import dev.karmakrafts.conventions.setProjectInfo
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

configureJava(rootProject.libs.versions.java)

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
        publishLibraryVariants("release")
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
                api(libs.kotlinx.io.core)
                api(libs.kotlinx.io.bytestring)
                api(projects.lognityApi)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.stately.common)
                implementation(libs.stately.collections)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val jvmAndAndroidMain by creating { dependsOn(commonMain) }
        jvmMain { dependsOn(jvmAndAndroidMain) }
        androidMain { dependsOn(jvmAndAndroidMain) }
        val webMain by creating { dependsOn(commonMain) }
        jsMain { dependsOn(webMain) }
        wasmJsMain { dependsOn(webMain) }
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
    setProjectInfo("Lognity Core", "Lightweight logging implementation for Kotlin/Multiplatform")
}