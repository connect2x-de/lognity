@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import de.connect2x.conventions.configureJava
import de.connect2x.conventions.defaultCompilerOptions
import de.connect2x.conventions.setProjectInfo
import de.connect2x.conventions.withAndroidLibrary
import de.connect2x.conventions.withJvm
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.findKaptConfiguration

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.android.library)
    alias(sharedLibs.plugins.kotlin.kapt)
    alias(sharedLibs.plugins.mavenPublish)
}

configureJava(sharedLibs.versions.targetJvm)

kotlin {
    defaultCompilerOptions()
    withSourcesJar()
    withAndroidLibrary("$group.slf4j")
    withJvm()
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

publishing {
    publications.withType<MavenPublication> {
        pom {
            setProjectInfo(
                name = "Lognity SLF4J",
                description = "Lognity API bridge for SLF4J",
                repository = "connect2x/lognity"
            )
        }
    }
}