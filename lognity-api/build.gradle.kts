import de.connect2x.conventions.configureJava
import de.connect2x.conventions.defaultCompilerOptions
import de.connect2x.conventions.setProjectInfo
import de.connect2x.conventions.withAndroidLibrary
import de.connect2x.conventions.withJs
import de.connect2x.conventions.withJvm
import de.connect2x.conventions.withNative
import de.connect2x.conventions.withWasm
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.android.library)
    alias(sharedLibs.plugins.kotlin.serialization)
    alias(sharedLibs.plugins.mavenPublish)
}

configureJava(libs.versions.java)

@OptIn(ExperimentalWasmDsl::class) //
kotlin {
    defaultCompilerOptions()
    withSourcesJar()
    withAndroidLibrary()
    withJvm()
    withNative()
    withJs {
        browser()
        nodejs()
    }
    withWasm {
        browser()
        nodejs()
    }
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain {
            dependencies {
                api(sharedLibs.kotlinx.io.core)
                api(sharedLibs.kotlinx.io.bytestring)
                implementation(sharedLibs.kotlinx.coroutines.core)
                implementation(sharedLibs.kotlinx.datetime)
                implementation(sharedLibs.kotlinx.serialization.core)
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            setProjectInfo(
                name = "Lognity API",
                description = "Lightweight logging API for Kotlin/Multiplatform",
                repository = "https://gitlab.com/connect2x/lognity"
            )
        }
    }
}