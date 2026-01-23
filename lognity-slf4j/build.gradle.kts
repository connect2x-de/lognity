@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import de.connect2x.conventions.configureJava
import de.connect2x.conventions.defaultCompilerOptions
import de.connect2x.conventions.setProjectInfo
import de.connect2x.conventions.withAndroid
import de.connect2x.conventions.withJvm
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.findKaptConfiguration

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.android.library)
    alias(sharedLibs.plugins.kotlin.kapt)
    alias(sharedLibs.plugins.mavenPublish)
}

configureJava(libs.versions.java)

kotlin {
    withSourcesJar()
    withJvm()
    withAndroid()
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndAndroid") {
                withJvm()
                withAndroidTarget()
            }
        }
    }
    defaultCompilerOptions()
    sourceSets {
        commonMain {
            dependencies {
                api(projects.lognityApi)
            }
        }
        @Suppress("UNUSED") //
        val jvmAndAndroidMain by getting {
            dependencies {
                api(sharedLibs.slf4j.api)
                implementation(sharedLibs.autoService)
            }
        }
    }
}

val kaptConfig = findKaptConfiguration("main")!!

dependencies {
    kaptConfig(sharedLibs.autoService)
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
                name = "Lognity SLF4J",
                description = "Lognity API bridge for SLF4J",
                repository = "https://gitlab.com/connect2x/lognity"
            )
        }
    }
}