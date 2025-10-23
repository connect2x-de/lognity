import net.folivo.lognity.gradle.setProjectInfo
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}

@OptIn(ExperimentalWasmDsl::class) //
kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
    withSourcesJar()
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    mingwX64()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    androidNativeX64()
    androidNativeArm64()
    androidNativeArm32()
    jvm()
    androidTarget {
        publishLibraryVariants("release")
    }
    js {
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
    applyDefaultHierarchyTemplate()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.lognityApi)
                api(libs.ktor.server.core)
            }
        }
        val jvmAndAndroidMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(projects.lognitySlf4j)
            }
        }
        androidMain { dependsOn(jvmAndAndroidMain) }
        jvmMain { dependsOn(jvmAndAndroidMain) }
    }
}

android {
    namespace = "$group.${rootProject.name}"
    compileSdk = libs.versions.androidCompileSDK.get().toInt()
    defaultConfig {
        minSdk = libs.versions.androidMinimalSDK.get().toInt()
    }
}

publishing {
    setProjectInfo("Lognity Ktor", "Ktor integration for the Lognity logging API")
}