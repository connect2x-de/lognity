import de.connect2x.conventions.configureJava
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
}

configureJava(libs.versions.java)

@OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class) //
kotlin {
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
    js {
        useEsModules()
        nodejs {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
    wasmJs {
        useEsModules()
        nodejs {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries {
            executable {
                entryPoint = "${rootProject.group}.example.main"
            }
        }
    }
    applyDefaultHierarchyTemplate {
        common {
            group("nonWeb") {
                withJvm()
                withAndroidTarget()
                withNative()
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.lognityApi)
                implementation(projects.lognityCore)
                implementation(projects.lognityCoreConfig)
            }
        }
    }
}