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

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class) //
kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
    jvm {
        binaries {
            executable {
                mainClass = "${rootProject.group}.example.MainKt"
            }
        }
    }
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    mingwX64()
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries {
            executable {
                entryPoint = "${rootProject.group}.example.main"
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.lognityApi)
                implementation(projects.lognityCore)
                implementation(projects.lognityConfig)
            }
        }
    }
}