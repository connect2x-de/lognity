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