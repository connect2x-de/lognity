package net.folivo.lognity.gradle

import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

fun Provider<MinimalExternalModuleDependency>.asAAR(): Provider<String> {
    return map { "$it@aar" }
}