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

import net.folivo.lognity.gradle.configureJava
import net.folivo.lognity.gradle.setProjectInfo
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.findKaptConfiguration

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.android.library)
}

configureJava(rootProject.libs.versions.java)

kotlin {
    withSourcesJar()
    jvm()
    androidTarget {
        publishLibraryVariants("release")
    }
    applyDefaultHierarchyTemplate()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.lognityApi)
            }
        }
        val jvmAndAndroidMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(libs.slf4j.api)
                implementation(libs.autoService)
            }
        }
        val androidMain by getting { dependsOn(jvmAndAndroidMain) }
        val jvmMain by getting { dependsOn(jvmAndAndroidMain) }
    }
}

val kaptConfig = findKaptConfiguration("main")!!

dependencies {
    kaptConfig(libs.autoService)
}

android {
    namespace = "$group.${rootProject.name}"
    compileSdk = libs.versions.androidCompileSDK.get().toInt()
    defaultConfig {
        minSdk = libs.versions.androidMinimalSDK.get().toInt()
    }
}

publishing {
    setProjectInfo("Lognity SLF4j", "SLF4j integration for the Lognity logging API")
}