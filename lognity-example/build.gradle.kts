import de.connect2x.conventions.configureJava
import de.connect2x.conventions.defaultCompilerOptions
import de.connect2x.conventions.withJs
import de.connect2x.conventions.withJvm
import de.connect2x.conventions.withLinux
import de.connect2x.conventions.withMacos
import de.connect2x.conventions.withMingw
import de.connect2x.conventions.withWasm
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
}

configureJava(libs.versions.java)

@OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class) //
kotlin {
    defaultCompilerOptions()
    withJvm {
        binaries {
            executable {
                mainClass = "${rootProject.group}.example.MainKt"
            }
        }
    }
    withLinux()
    withMacos()
    withMingw()
    withJs {
        useCommonJs()
        nodejs {
            binaries.executable()
            runTask { workingDir = layout.projectDirectory.asFile }
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
        browser {
            binaries.executable()
            runTask {
                mainOutputFileName = "${project.name}.js"
            }
            webpackTask {
                mainOutputFileName = "${project.name}.js"
            }
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
    withWasm {
        nodejs {
            binaries.executable()
            runTask { workingDir = layout.projectDirectory.asFile }
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
        browser {
            binaries.executable()
            runTask {
                mainOutputFileName = "${project.name}.js"
            }
            webpackTask {
                mainOutputFileName = "${project.name}.js"
            }
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
                implementation(sharedLibs.kotlinx.coroutines.core)
            }
        }
    }
}