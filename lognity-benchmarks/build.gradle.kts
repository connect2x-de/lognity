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

plugins {
    alias(sharedLibs.plugins.kotlin.multiplatform)
    alias(sharedLibs.plugins.kotlinx.benchmark)
}

configureJava(sharedLibs.versions.targetJvm)

@OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class) //
kotlin {
    defaultCompilerOptions()
    withJvm()
    withLinux()
    withMacos()
    withMingw()
    withIos()
    withWeb {
        useCommonJs()
        withNodeJs()
        withBrowser()
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
                implementation(sharedLibs.kotlinx.benchmark.runtime)
            }
        }
    }
}

benchmark {
    targets {
        register("jvm")
        register("linuxX64")
        register("linuxArm64")
        register("macosArm64")
        register("macosX64")
        register("mingwX64")
        register("js")
        register("wasmJs")
    }
    configurations {
        named("main") {
            iterationTime = 50
            iterationTimeUnit = "ms"
            iterations = 50
            warmups = 50
        }
    }
}