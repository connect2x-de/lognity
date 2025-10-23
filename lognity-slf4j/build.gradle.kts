import net.folivo.lognity.gradle.setProjectInfo
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.findKaptConfiguration

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.android.library)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
    withSourcesJar()
    jvm()
    androidTarget {
        publishLibraryVariants("release")
    }
    applyDefaultHierarchyTemplate()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.lognityApi)
            }
        }
        val jvmAndAndroidMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(libs.slf4j.api)
                implementation(libs.autoService)
            }
        }
        val androidMain by getting { dependsOn(jvmAndAndroidMain) }
        val jvmMain by getting { dependsOn(jvmAndAndroidMain) }
    }
}

val kaptConfig = findKaptConfiguration("main")!!

dependencies {
    kaptConfig(libs.autoService)
}

android {
    namespace = "$group.${rootProject.name}"
    compileSdk = libs.versions.androidCompileSDK.get().toInt()
    defaultConfig {
        minSdk = libs.versions.androidMinimalSDK.get().toInt()
    }
}

publishing {
    setProjectInfo("Lognity SLF4j", "SLF4j integration for the Lognity logging API")
}