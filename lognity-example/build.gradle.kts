import de.connect2x.conventions.configureJava
import de.connect2x.conventions.defaultCompilerOptions
import de.connect2x.conventions.withBrowser
import de.connect2x.conventions.withIos
import de.connect2x.conventions.withJvm
import de.connect2x.conventions.withLinux
import de.connect2x.conventions.withMacos
import de.connect2x.conventions.withMingw
import de.connect2x.conventions.withNodeJs
import de.connect2x.conventions.withWeb
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
}

configureJava(sharedLibs.versions.targetJvm)

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
    withIos {
        binaries.framework {
            baseName = "LognityExample"
            isStatic = true
        }
    }
    withWeb {
        useCommonJs()
        withNodeJs {
            binaries.executable()
            runTask { workingDir = layout.projectDirectory.dir("src/webMain/resources").asFile }
        }
        withBrowser {
            binaries.executable()
            runTask {
                mainOutputFileName = "${project.name}.js"
            }
            webpackTask {
                mainOutputFileName = "${project.name}.js"
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