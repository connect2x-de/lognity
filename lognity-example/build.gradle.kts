import de.connect2x.conventions.configureJava
import de.connect2x.conventions.defaultCompilerOptions
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
}

configureJava(libs.versions.java)

@OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class) //
kotlin {
    defaultCompilerOptions()
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
        compilerOptions {
            sourceMap = true
            sourceMapEmbedSources = JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS
        }
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
    wasmJs {
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