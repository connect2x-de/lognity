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
    alias(sharedLibs.plugins.kotlin.serialization)
    alias(sharedLibs.plugins.android.library)
    alias(sharedLibs.plugins.mavenPublish)
}

configureJava(sharedLibs.versions.targetJvm)

kotlin {
    defaultCompilerOptions()
    withSourcesJar()
    withAndroidLibrary("$group.logstash.config")
    withJvm()
    withNative()
    sourceSets {
        commonMain {
            dependencies {
                api(projects.lognityConfig)
                api(projects.lognityLogstash)
                implementation(sharedLibs.kotlinx.serialization.core)
                implementation(sharedLibs.kotlinx.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.lognityCoreConfig)
                implementation(sharedLibs.kotlin.test)
                implementation(sharedLibs.kotlinx.coroutines.test)
            }
        }
        jvmTest {
            resources.srcDir(rootProject.layout.projectDirectory.dir("schema"))
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            setProjectInfo(
                name = "Lognity Logstash Config",
                description = "Lognity Config extension for Lognity Logstash",
                repository = "connect2x/lognity"
            )
        }
    }
}