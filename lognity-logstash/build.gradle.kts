@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import de.connect2x.conventions.configureJava
import de.connect2x.conventions.defaultCompilerOptions
import de.connect2x.conventions.setProjectInfo
import de.connect2x.conventions.withAndroidLibrary
import de.connect2x.conventions.withJvm
import de.connect2x.conventions.withNative
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.android.library)
    alias(sharedLibs.plugins.kotlin.kapt)
    alias(sharedLibs.plugins.mavenPublish)
    alias(sharedLibs.plugins.kotlin.serialization)
}

configureJava(sharedLibs.versions.targetJvm)

kotlin {
    defaultCompilerOptions()
    withSourcesJar()
    withAndroidLibrary("$group.logstash")
    withJvm()
    withNative()
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
                api(libs.ktor.network)
                api(libs.ktor.network.tls)
                api(sharedLibs.kotlinx.coroutines.core)
                implementation(sharedLibs.kotlinx.serialization.core)
                api(sharedLibs.kotlinx.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(sharedLibs.kotlin.test)
                implementation(sharedLibs.kotlinx.coroutines.test)
            }
        }
        named("jvmAndAndroidTest") {
            dependencies {
                implementation(libs.ktor.network.tls.certificates)
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            setProjectInfo(
                name = "Lognity Logstash",
                description = "Lognity appender for Logstash TCP logging",
                repository = "connect2x/lognity"
            )
        }
    }
}