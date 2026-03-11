import de.connect2x.conventions.configureJava
import de.connect2x.conventions.defaultCompilerOptions
import de.connect2x.conventions.enableAbiChecker
import de.connect2x.conventions.setProjectInfo
import de.connect2x.conventions.withAndroidLibrary
import de.connect2x.conventions.withBrowser
import de.connect2x.conventions.withJvm
import de.connect2x.conventions.withNative
import de.connect2x.conventions.withNodeJs
import de.connect2x.conventions.withWeb

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.android.library)
    alias(sharedLibs.plugins.kotlin.serialization)
    alias(sharedLibs.plugins.mavenPublish)
}

configureJava(sharedLibs.versions.targetJvm)

kotlin {
    enableAbiChecker("InternalLoggingApi", "${rootProject.group}.api")
    defaultCompilerOptions()
    withSourcesJar()
    withAndroidLibrary("$group.api")
    withJvm()
    withNative()
    withWeb {
        withBrowser()
        withNodeJs()
    }
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain {
            dependencies {
                api(sharedLibs.kotlinx.io.core)
                api(sharedLibs.kotlinx.io.bytestring)
                api(sharedLibs.jetbrains.annotations)
                implementation(sharedLibs.kotlinx.coroutines.core)
                implementation(sharedLibs.kotlinx.datetime)
                implementation(sharedLibs.kotlinx.serialization.core)
            }
        }
        commonTest {
            dependencies {
                implementation(sharedLibs.kotlin.test)
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
                repository = "connect2x/lognity"
            )
        }
    }
}